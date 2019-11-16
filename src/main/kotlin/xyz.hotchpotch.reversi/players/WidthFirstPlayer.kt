package xyz.hotchpotch.reversi.players

import xyz.hotchpotch.reversi.*
import xyz.hotchpotch.reversi.framework.Player
import xyz.hotchpotch.reversi.framework.PlayerFactory
import java.time.Instant
import java.util.*
import kotlin.properties.Delegates

/** 今回の手に費やせる時間を計算する際の余裕代 */
private const val MARGIN: Long = 30

/**
 * 幅優先探索による最善手を選択するプレーヤーです。
 *
 * @param color このプレーヤーの石の色
 * @param millisAtTurn 一手当たりの制限時間（ミリ秒）
 * @param evaluator リバーシ盤に対する評価関数
 */
class WidthFirstPlayer(
        private val color: Color,
        private val millisAtTurn: Long,
        private val evaluator: (Board, Color) -> Double = Evaluators::evaluateBoard5
) : Player {

    companion object : PlayerFactory {
        override fun create(color: Color, millisInGame: Long, millisAtTurn: Long): Player =
                WidthFirstPlayer(color, millisAtTurn)
    }

    override fun choosePoint(board: Board, millisInGame: Long): Point? {
        val root = Node(null, null, board, color)
        val nodesInProcess: Queue<Node> = ArrayDeque()
        nodesInProcess.add(root)

        val deadline: Instant = deadline(board, millisInGame)
        do {
            val currNode: Node = nodesInProcess.remove()
            nodesInProcess.addAll(currNode.search())
        } while (nodesInProcess.isNotEmpty() && Instant.now() < deadline)

        return root.bestChild!!.previouslyChosen
    }

    /** 今回の手に費やせる時間を計算し、探索を切り上げるデッドラインを返す。 */
    private fun deadline(board: Board, millisInGame: Long): Instant {
        val remainedMyTurns = (Point.values.filter { board[it] === null }.count() + 1) / 2
        val millisForThisTurn: Long = java.lang.Long.min(millisAtTurn, millisInGame / remainedMyTurns) - MARGIN
        return Instant.now().plusMillis(millisForThisTurn)
    }

    /**
     * ゲームのある時点の状態を表すノードです。
     *
     * @param parent 親ノード。ルートノードの場合は null
     * @param previouslyChosen 親ノードからこのノードに遷移する際に選択された手
     * @param currBoard 現在のリバーシ盤
     * @param currColor 次の手を選択すべきプレーヤーの石の色
     */
    // お勉強MEMO: WidthFirstPlayerのメンバ（color, evaluator）にアクセスしたいため、インナークラスを使う。
    private inner class Node(
            private val parent: Node?,
            val previouslyChosen: Point?,
            private val currBoard: Board,
            private val currColor: Color) {

        // 設計メモ：
        // このプレーヤーは時間が許す限り幅優先探索で次の手を読み進める。
        // そして探索の最先端ノードのリバーシ盤を評価関数で評価し、
        // ミニマックス法で直近ノードのスコアを算出して手を決める。
        //
        // 本来、コストのかかるリバーシ盤の評価処理はリーフノード（最先端ノード）のみで実施べきだが、
        // 以下の理由から、途中ノードを含む全ノードで評価計算を行ってその都度親ノードに結果を遡及させる
        // という方法を採用した。
        //   1.最後に一度に評価処理を行って時間切れになるリスクを減らすため
        //   2.ソース簡略化のため
        //   3.お勉強としてobservableを使ってみたかったため

        /** currColorにとって最も良い一手先の状態 */
        // お勉強MEMO: observableを使ってみる。
        var bestChild: Node? by Delegates.observable<Node?>(null) { _, _, new ->
            // bestChildが更新された場合は、scoreも更新する。
            if (score != new!!.score) score = new.score
        }

        /** このノードのcolorから見たスコア */
        // お勉強MEMO: observableを使ってみる。
        var score: Double by Delegates.observable(evaluator.invoke(currBoard, color)) { _, _, _ ->
            // このノードのスコアが更新された場合は、親ノードのスコアも更新する。
            updateParentBest()
        }

        init {
            // observableは初期値設定時は発火しないため、 initブロックでの処理が必要。
            // もうちょっとの工夫でもうちょっとスマートになる気がするんだけどなぁ・・・
            if (parent !== null) {
                if (parent.bestChild === null) parent.bestChild = this
                else updateParentBest()
            }
        }

        private fun updateParentBest() {
            if (parent === null) return
            when {
                parent.currColor === color && parent.score < score -> parent.bestChild = this
                parent.currColor !== color && score < parent.score -> parent.bestChild = this
            }
        }

        /** 一手先の状態を探索して返します。 */
        fun search(): Set<Node> {
            val puttables: Set<Point> = currBoard.puttables(currColor)

            return when {

                // 石を置ける位置がある場合は、そのそれぞれに対応するノードを生成する。
                puttables.isNotEmpty() -> puttables
                        .map { Node(this, it, currBoard + Move(currColor, it), currColor.reversed()) }
                        .toSet()

                // 自身は石を置けないが相手は置ける場合は、パスした状態が子ノードとなる。
                currBoard.canPut(currColor.reversed()) ->
                    setOf(Node(this, null, currBoard, currColor.reversed()))

                // 上記に当てはまらない場合はゲーム終了状態であるため、子ノードは無い。
                else -> emptySet()
            }
        }
    }
}

/**
 * リバーシ盤に対する評価関数を集めたものです。
 * color から見た時の board の有利さを表すスコアを計算します。
 */
private object Evaluators {

    // TODO: 評価関数をまじめに設計する

    /** 既に置かれている石の数に基づいてスコアを計算する評価関数です。 */
    fun evaluateBoard1(board: Board, color: Color): Int =
            board.count(color) - board.count(color.reversed())

    /** 石を置ける位置の数に基づいてスコアを計算する評価関数です。 */
    fun evaluateBoard2(board: Board, color: Color): Int =
            board.puttables(color).size - board.puttables(color.reversed()).size

    /**
     * [evaluateBoard1] と [evaluateBoard2] の混合でスコアを計算する評価関数です。
     * ゲーム序盤は [evaluateBoard2] に、
     * ゲーム終盤は [evaluateBoard1] にウェイトを置いてスコアを算出します。
     */
    fun evaluateBoard3(board: Board, color: Color): Double {
        val stones: Int = Point.values.count { board[it] !== null }
        // テキトーなロジック。妥当性の根拠なし。
        return ((evaluateBoard1(board, color) * stones) +
                (evaluateBoard2(board, color) * (Point.HEIGHT * Point.WIDTH - stones))).toDouble() /
                (Point.HEIGHT * Point.WIDTH)
    }

    /** どんなリバーシ盤にもスコア0.0を返す評価関数です。　*/
    fun evaluateBoard4(board: Board, color: Color): Double = 0.0

    /** リバーシ盤のマス目ごとにウェイトを設定し、占有するマス目のウェイトに応じてスコアを計算する評価関数です。 */
    fun evaluateBoard5(board: Board, color: Color): Double {
        assert(Point.HEIGHT == 8 && Point.WIDTH == 8)

        val weights: Array<Array<Int>> = arrayOf(
                // この内容も根拠なし。適当に置いた数字。
                arrayOf(10, -3, 7, 5, 5, 7, -3, 10),
                arrayOf(-3, -5, 3, 2, 2, 3, -5, -3),
                arrayOf(7, 3, 8, 5, 5, 8, 3, 7),
                arrayOf(5, 2, 5, 3, 3, 5, 2, 5),
                arrayOf(5, 2, 5, 3, 3, 5, 2, 5),
                arrayOf(7, 3, 8, 5, 5, 8, 3, 7),
                arrayOf(-3, -5, 3, 2, 2, 3, -5, -3),
                arrayOf(10, -3, 7, 5, 5, 7, -3, 10)
        )
        val myScore: Int = Point.values
                .filter { board[it] === color }
                .map { weights[it.i][it.j] }
                .sum()
        val hisScore: Int = Point.values
                .filter { board[it] === color.reversed() }
                .map { weights[it.i][it.j] }
                .sum()
        return (myScore - hisScore).toDouble()
    }
}

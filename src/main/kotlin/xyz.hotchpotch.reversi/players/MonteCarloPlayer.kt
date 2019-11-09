package xyz.hotchpotch.reversi.players

import xyz.hotchpotch.reversi.*
import xyz.hotchpotch.reversi.framework.Player
import xyz.hotchpotch.reversi.framework.PlayerFactory
import java.lang.Long.min
import java.time.Instant
import java.util.stream.Stream

/** 1サイクル当たりの試行（プレイアウト）回数 */
// 残り時間や処理性能とかに応じてここを可変にするとより良くなるんだろうけど
// 面倒なので今回はこれで。
private const val TIMES: Int = 25
private const val MARGIN: Long = 45

/**
 * モンテカルロシミュレーションで最も勝率が高い手を選択するプレーヤーです。
 *
 * @param color このプレーヤーの石の色
 * @param millisInTurn 一手当たりの制限時間（ミリ秒）
 */
class MonteCarloPlayer(private val color: Color, private val millisInTurn: Long) : Player {

    /** 思考時間が無いときに判断を委譲するプレーヤー */
    private val proxy: Player = RandomPlayer.create(color, 0, 0)

    companion object : PlayerFactory {
        override fun create(color: Color, millisInGame: Long, millisInTurn: Long): Player =
                MonteCarloPlayer(color, millisInTurn)
    }

    /**
     * 石の置ける位置を候補手としてモンテカルロシミュレーションを行い、
     * 最も勝率の高い手を選択して返します。
     */
    override fun choosePoint(board: Board, millisInGame: Long): Point? {
        val now: Instant = Instant.now()

        // 石を置ける位置
        val availables = Point.values.filter { board.canPutAt(color, it) }

        // 石を置ける位置が0（パス）または1の場合は、直ちに手が決まる。
        if (availables.isEmpty()) return null
        else if (availables.size == 1) return availables[0]

        val totalWins: MutableMap<Point, Long> = mutableMapOf()

        // 時間がある限り、思考（試行）を繰り返す。
        val deadline: Instant = deadline(now, board, millisInGame)
        while (Instant.now() < deadline) {

            // 石を置ける位置それぞれについてプレイアウトをN回行い、その成績を積算する。
            val wins: Map<Point, Long> = availables.associateWith { playOutN(board, it) }
            availables.forEach { totalWins[it] = (totalWins[it] ?: 0) + (wins[it] ?: 0) }
        }

        // 最も勝ち数が多かった手を選択して返す。
        // maxBy を使うと同成績の位置が複数あったときに結果が偏る気がするけど、まぁいいや
        return if (totalWins.isNotEmpty()) totalWins.maxBy { it.value }!!.key
        // 時間がなく一度も施行できなかった場合はプロキシに委ねる。
        else proxy.choosePoint(board, millisInGame)
    }

    /** 今回の手に費やせる時間を計算し、最後の施行を始めるデッドラインを返す。 */
    private fun deadline(now: Instant, board: Board, millisInGame: Long): Instant {
        // このロジックは凝ろうと思えば色々と凝れるし、それで強くもなるが、
        // 面倒くさいので一旦これで
        val remainingMyTurns = (Point.values.filter { board[it] === null }.count() + 1) / 2
        val millisForThisTurn: Long = min(millisInTurn, millisInGame / remainingMyTurns) - MARGIN
        return now.plusMillis(millisForThisTurn)
    }

    /**
     * 現在のターンで自分が選択できる手の一つについて、プレイアウトを規定回数実施し、
     * 自身の色が勝利した回数を返します。
     *
     * @param currBoard 現在のリバーシ盤
     * @param candidate 現在のターンで選択できる位置のうちの一つ
     * @param times プレイアウトを行う回数
     */
    // 本当は勝利回数だけじゃなくて引き分けの回数も考慮すると精度が上がるが、今回はまぁ良しとする。
    private fun playOutN(currBoard: Board, candidate: Point, times: Int = TIMES): Long {
        assert(currBoard.canPutAt(color, candidate))

        val nextBoard: Board = currBoard + Move(color, candidate)

        // ここで並列化するのが一番良いんじゃないかなー・・・　というのは根拠のない想定
        return Stream.generate { nextBoard.toMutableBoard() }
                .parallel()
                .limit(times.toLong())
                .map { playOut1(it, color.reversed()) }
                .filter { it === color }
                .count()
    }

    /**
     * 指定されたリバーシ盤をランダムな手で更新し、末尾再帰的にゲーム終了まで進め、勝者の色を返します。
     *
     * @param board このプレイアウトのための変更可能リバーシ盤
     * @param currTurn 現在の手番の色
     * @return 勝者の色。引き分けの場合は null
     */
    // お勉強MEMO: せっかくなので末尾再帰（tailrec）ってやつを使ってみる。
    private tailrec fun playOut1(board: MutableBoard, currTurn: Color): Color? {
        if (!board.isGameOngoing()) return board.winner()

        val availables = Point.values.filter { board.canPutAt(currTurn, it) }
        if (availables.isNotEmpty()) board.apply(Move(currTurn, availables.random()))
        return playOut1(board, currTurn.reversed())
    }
}
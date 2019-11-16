package xyz.hotchpotch.reversi

/**
 * 読み取り専用のリバーシ盤を表します。
 */
interface Board {

    /** 指定された位置の石の色を返します。石が置かれていない場合は null を返します。 */
    // これは null を返す設計で妥当だと信じる。3値の列挙型を定義するのはなんか嫌い。
    operator fun get(point: Point): Color?
}

// お勉強MEMO:
// インタフェースのメンバ関数でデフォルト実装を持つものについて、
// 拡張関数として実装した方が良いのはどんな場合か？？
//   - private にしたいものは拡張として実装（これは確定）
//   - オーバーライドを期待しないものは拡張として実装
//   - オーバーライドを期待するものはインタフェースのメンバとして実装
// かな？？
// 今回は実験として、デフォルト実装を持つものはすべて拡張として実装してみる。
// この方が、インタフェース内がすっきりし、実装（オーバーライド）すべきメンバが分かりやすくなる気がする。

/** このリバーシ盤上の指定された色の石の数を返します。 */
fun Board.count(color: Color): Int = Point.values.count { this[it] === color }

/** このリバーシ盤を同等の内容のマップに変換して返します。 */
private fun Board.toMap(): Map<Point, Color> =
        Point.values.filter { this[it] !== null }.associateWith { this[it]!! }

/**
 * このリバーシ盤に指定された手を適用して得られるリバーシ盤を新たに生成して返します。
 * @throws IllegalArgumentException 適用できない手が指定された場合
 */
operator fun Board.plus(move: Move): Board {
    require(canApply(move)) { "この手は適用できません。\n$this$move" }
    return if (move.isPass()) {
        BoardImpl(this)
    } else {
        val nextMap: MutableMap<Point, Color> = toMap().toMutableMap()
        val reversibles: Set<Point> = reversibles(move.color, move.point!!)
        reversibles.forEach { nextMap[it] = move.color }
        nextMap[move.point] = move.color
        BoardImpl(nextMap)
    }
}

/** このリバーシ盤の読み取り専用コピーを生成して返します。 */
// お勉強MEMO:
// 読み取り専用 ≠ 不変　ではあるものの、
// 読み取り専用ビューなら不変オブジェクトを返したいという気持ちがある。
// これは Java 脳なのかしら？？
fun Board.toBoard(): Board = boardOf(this)

/** このリバーシ盤の変更可能コピーを生成して返します。 */
fun Board.toMutableBoard(): MutableBoard = mutableBoardOf(this)

/**
 * 変更可能なリバーシ盤を表します。
 */
interface MutableBoard : Board {

    /**
     * 指定された手を適用してこのリバーシ盤を更新します。
     * @throws IllegalArgumentException 適用できない手が指定された場合
     */
    fun apply(move: Move)
}

/** ゲーム開始時の状態のリバーシ盤を生成して返します。 */
fun boardOf(): Board = BoardImpl()

/** ゲーム開始時の状態のリバーシ盤を生成して返します。 */
fun mutableBoardOf(): MutableBoard = MutableBoardImpl()

/** 指定されたリバーシ盤と同じ内容のリバーシ盤を生成して返します。 */
fun boardOf(original: Board): Board = BoardImpl(original)

/** 指定されたリバーシ盤と同じ内容のリバーシ盤を生成して返します。 */
fun mutableBoardOf(original: Board): MutableBoard = MutableBoardImpl(original)

/** 任意の内容のリバーシ盤を生成して返します。 */
// テストのためだけの関数。
// テストのためだけにバックドアを開けるのはバッドノウハウと分かりつつ・・・
// 他にやりようはあるんだろうか？？
internal fun boardOf(map: Map<Point, Color>): Board = BoardImpl(map)

/** ゲーム開始時の石の配置を表すマップ */
private val initMap: Map<Point, Color> = mapOf(
        Point[(Point.HEIGHT - 1) / 2, (Point.WIDTH - 1) / 2] to Color.WHITE,
        Point[Point.HEIGHT / 2, Point.WIDTH / 2] to Color.WHITE,
        Point[(Point.HEIGHT - 1) / 2, Point.WIDTH / 2] to Color.BLACK,
        Point[Point.HEIGHT / 2, (Point.WIDTH - 1) / 2] to Color.BLACK
)

/**
 * 読み取り専用リバーシ盤の標準的な実装です。
 */
// TODO: そのうちビット演算ベースの高パフォーマンス実装に切り替える
private open class BoardImpl : Board {

    /** このリバーシ盤の石の配置を保持するマップ */
    // お勉強MEMO:
    // このプロパティを open にせず、かつ BoardImpl と共用できるよう、
    // BoardImpl.map は Map として定義し MutableBoardImpl.map で MutableMap にオーバーライドするのではなく、
    // 最初から MutableMap にしてしまうことにした。
    protected val map: MutableMap<Point, Color>

    constructor() {
        map = initMap.toMutableMap()
    }

    constructor(map: Map<Point, Color>) {
        this.map = map.toMutableMap()
    }

    constructor(original: Board) {
        map = when (original) {
            is BoardImpl -> original.map.toMutableMap()
            else -> original.toMap().toMutableMap()
        }
    }

    override fun get(point: Point): Color? = map[point]

    override fun equals(other: Any?): Boolean = when (other) {
        is BoardImpl -> map == other.map
        is Board -> map == other.toMap()
        else -> false
    }

    // お勉強MEMO:
    // 本当は hashCode(), equals(Any?) も Board インタフェースのデフォルト実装なり拡張なりとして
    // 全 [Board] 実装クラスで統一したい。じゃないと未知の [Board] 実装クラスとの間で不整合が生じる。
    // これ、どうすれば良いんだろう？
    override fun hashCode(): Int = map.hashCode()

    override fun toString(): String {
        val str: StringBuilder = StringBuilder("  ")
        for (j in 0 until Point.WIDTH) {
            str.append('a' + j).append(' ')
        }
        str.appendln()

        for (i in 0 until Point.HEIGHT) {
            str.append(i + 1).append(' ')
            for (j in 0 until Point.WIDTH) {
                str.append(get(Point[i, j]) ?: "・")
            }
            str.appendln()
        }
        return str.toString()
    }
}

/**
 * 編集可能リバーシ盤の標準的な実装です。
 */
private class MutableBoardImpl : BoardImpl, MutableBoard {
    constructor() : super()
    constructor(original: Board) : super(original)

    override fun apply(move: Move) {
        require(canApply(move)) { "この手は適用できません。\n$this$move" }
        if (!move.isPass()) {
            val reversibles: Set<Point> = reversibles(move.color, move.point!!)
            reversibles.forEach { map[it] = move.color }
            map[move.point] = move.color
        }
    }
}

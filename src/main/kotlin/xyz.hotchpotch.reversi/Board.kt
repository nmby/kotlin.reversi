package xyz.hotchpotch.reversi

/**
 * 読み取り専用のリバーシ盤を表します。
 */
interface Board {

    /**
     * @return 指定された位置の石の色を返します。石が置かれていない場合は null を返します。
     */
    operator fun get(point: Point): Color?

    /**
     * @return このリバーシ盤に指定された手を適用して得られるリバーシ盤を新たに生成して返します。
     * @throws IllegalArgumentException 適用できない手が指定された場合
     */
    operator fun plus(move: Move): Board

    /**
     * @return このリバーシ盤の読み取り専用コピーを生成して返します。
     */
    // お勉強MEMO:
    // 読み取り専用 ≠ 不変　ではあるものの、
    // 読み取り専用ビューなら不変オブジェクトを返したいという気持ちがある。
    // これは Java 脳なのかしら？？
    fun toBoard(): Board = boardOf(this)

    /**
     * @return このリバーシ盤の変更可能コピーを生成して返します。
     */
    fun toMutableBoard(): MutableBoard = mutableBoardOf(this)

    /**
     * @return このリバーシ盤上の指定された色の石の数を返します。
     */
    fun count(color: Color): Int = Point.values().count { this[it] === color }
}

/**
 * 変更可能なリバーシ盤を表します。
 */
interface MutableBoard : Board {

    /**
     * 指定された手を適用してこのリバーシ版を更新します。
     * @throws IllegalArgumentException 適用できない手が指定された場合
     */
    fun apply(move: Move)
}

/**
 * @return ゲーム開始時の状態のリバーシ盤を生成して返します。
 */
fun boardOf(): Board = BoardImpl()

/**
 * @return 指定されたリバーシ盤と同じ内容のリバーシ盤を生成して返します。
 */
fun boardOf(original: Board): Board = BoardImpl(original)

/**
 * @return ゲーム開始時の状態のリバーシ盤を生成して返します。
 */
fun mutableBoardOf(): MutableBoard = MutableBoardImpl()

/**
 * @return 指定されたリバーシ盤と同じ内容のリバーシ盤を生成して返します。
 */
fun mutableBoardOf(original: Board): MutableBoard = MutableBoardImpl(original)

// テストのために任意の状態のリバーシ盤を生成するための関数。
// お勉強MEMO: テストのためだけにバックドアを開けるやつ、バッドノウハウなんだと思うけど・・・
internal fun boardOf(map: Map<Point, Color>): Board = BoardImpl(map)

private fun Board.toMap(): Map<Point, Color> =
        Point.values().filter { this[it] !== null }.associateWith { this[it]!! }

private val initMap: Map<Point, Color> = mapOf(
        Point[(Point.HEIGHT - 1) / 2, (Point.WIDTH - 1) / 2] to Color.WHITE,
        Point[Point.HEIGHT / 2, Point.WIDTH / 2] to Color.WHITE,
        Point[(Point.HEIGHT - 1) / 2, Point.WIDTH / 2] to Color.BLACK,
        Point[Point.HEIGHT / 2, (Point.WIDTH - 1) / 2] to Color.BLACK
)

private open class BoardImpl : Board {

    // お勉強MEMO:
    // このプロパティを open にせず、かつ BoardImpl と共用できるよう、
    // BoardImpl.map は Map として MutableBoardImpl.map は MutableMap にオーバーライドするのではなく、
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

    override operator fun plus(move: Move): Board {
        require(canApply(move)) { "この手は適用できません。\n$this$move" }
        return if (move.isPass()) {
            BoardImpl(map)
        } else {
            val nextMap: MutableMap<Point, Color> = map.toMutableMap()
            val reversibles: Set<Point> = reversibles(move.color, move.point!!)
            reversibles.forEach { nextMap[it] = move.color }
            nextMap[move.point] = move.color
            BoardImpl(nextMap)
        }
    }

    override fun equals(other: Any?): Boolean = when (other) {
        is BoardImpl -> map == other.map
        is Board -> map == other.toMap()
        else -> false
    }

    override fun hashCode(): Int = map.hashCode()

    override fun toString(): String {
        val str: StringBuilder = StringBuilder("  ")
        (0 until Point.WIDTH).forEach { str.append('a' + it).append(' ') }
        str.appendln()

        (0 until Point.HEIGHT).forEach { i ->
            str.append(i + 1).append(' ')
            (0 until Point.WIDTH).forEach { j ->
                str.append(get(Point[i, j]) ?: "・")
            }
            str.appendln()
        }
        return str.toString()
    }
}

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

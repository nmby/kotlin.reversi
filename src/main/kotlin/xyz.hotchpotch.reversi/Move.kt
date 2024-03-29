package xyz.hotchpotch.reversi

/**
 * リバーシの手を表す不変クラスです。
 *
 * @param color 石の色
 * @param point 石を置く位置。パスの場合は null
 */
data class Move constructor(val color: Color, val point: Point?) {

    /** この手がパスを表す場合に true を返します。 */
    fun isPass(): Boolean = point === null

    override fun toString(): String = "$color: %s".format(point ?: "PASS")
}

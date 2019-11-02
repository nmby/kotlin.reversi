package xyz.hotchpotch.reversi

// お勉強MEMO:
// こういう Java で言う　private static なメソッドを
// トップレベルに置くのはいかがなのかしら・・・？
private fun isValidIdx(i: Int, j: Int): Boolean =
        i in 0 until Point.HEIGHT && j in 0 until Point.WIDTH

private fun idxToOrdinal(i: Int, j: Int): Int = i * Point.WIDTH + j

/**
 * リバーシ盤上の位置を表す不変クラスです。
 * 同じ位置を表す Point オブジェクトは同一インスタンスであることが保証されます。
 */
class Point private constructor(ordinal: Int) {

    // MEMO:
    // 本当は、いちいち Point.values.forEach { ～ }　などと書くのはダサいので、
    // Point.forEach { ～ } などと書けるように、
    //     companion object: Iterable<Point> by values {
    //         private val values: List<Point> = (0 until HEIGHT * WIDTH).map { Point[it] }.toList()
    //     }
    // としたかった。
    // しかし、コンパイルは正常に通るものの、Point.forEach { ～ } などを実行すると
    // 実行時に NullPointerException が発生してしまう。
    // 初期化の順番？タイミング？の問題だと推測するが、詳細な原理は要お勉強。
    companion object {

        /** 座標平面（つまりリバーシ盤）の高さ */
        const val HEIGHT = 8

        /** 座標平面（つまりリバーシ盤）の幅 */
        const val WIDTH = 8

        private val values: List<Point> = (0 until HEIGHT * WIDTH)
                .map { Point(it) }
                .toList()

        // 冗長ではあるが、アクセス効率を優先させる。
        private val map: Map<String, Point> = values.associateBy(Point::pos)

        /**
         * @return 全ての [Point] インスタンスが格納されたリストを返します。
         */
        // MEMO: values プロパティをそのまま public にしてしまっても良いのだが、
        // Enum.values() とスタイルを合わせることにした。
        fun values(): List<Point> = values

        /**
         * @return [i, j] 形式で指定された位置の Point インスタンスを返します。
         * @throws IndexOutOfBoundsException [i, j] が範囲外の場合
         */
        operator fun get(i: Int, j: Int): Point {
            require(isValidIdx(i, j)) { "($i, $j)" }
            return values[idxToOrdinal(i, j)]
        }

        /**
         * @return ["a1"] 形式で指定された位置の Point インスタンスを返します。
         * @throws IllegalArgumentException posが不正な形式または範囲外の場合
         */
        operator fun get(pos: String): Point = map[pos] ?: throw IllegalArgumentException(pos)
    }

    /** 縦座標 */
    val i: Int = ordinal / WIDTH

    /** 横座標 */
    val j: Int = ordinal % WIDTH

    /** "a1"～"h8" 形式の座標 */
    val pos: String = "%c%d".format('a' + j, 1 + i)

    /**
     * @return この点の指定された方向の次の点。次の点が無い場合は null
     */
    operator fun plus(direction: Direction): Point? {
        val ni: Int = i + direction.di
        val nj: Int = j + direction.dj
        return if (isValidIdx(ni, nj)) get(ni, nj) else null
    }

    override fun toString(): String = pos
}
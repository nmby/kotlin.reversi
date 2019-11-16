package xyz.hotchpotch.reversi

/** (i, j) 形式の座標が範囲内かを返します。 */
// お勉強MEMO:
// こういう Java で言う　private static なメソッドを
// トップレベルに置くのはいかがなのかしら・・・？
private fun isValidIdx(i: Int, j: Int): Boolean =
        i in 0 until Point.HEIGHT && j in 0 until Point.WIDTH

/** (i, j) 形式の座標を序数に変換します。 */
private fun idxToOrdinal(i: Int, j: Int): Int = i * Point.WIDTH + j

/**
 * リバーシ盤上の位置を表す不変クラスです。
 * 同じ位置を表す Point オブジェクトは同一インスタンスであることが保証されます。
 */
// 実装メモ：
// [Point] は本質的に列挙だが、
//   - 64個のメンバを列挙して実装するのはダサすぎる
//   - HEIGHT, WIDTH で定義できるようにしたい
// ということで、通常のクラスとして実装している。
class Point private constructor(private val ordinal: Int) : Comparable<Point> {

    companion object {

        /** 座標平面（つまりリバーシ盤）の高さ */
        const val HEIGHT = 8

        /** 座標平面（つまりリバーシ盤）の幅 */
        const val WIDTH = 8

        /** 全ての [Point] インスタンスを保持するリスト */
        val values: List<Point> = (0 until HEIGHT * WIDTH).map { Point(it) }
            get() = field.toList()

        /** 全ての [Point] インスタンスを保持するマップ */
        // 冗長ではあるが、アクセス効率を優先させる。
        private val map: Map<String, Point> = values.associateBy(Point::pos)

        /**
         * (i, j) 形式で指定された位置の Point インスタンスを返します。
         * @throws IndexOutOfBoundsException (i, j) が範囲外の場合
         */
        operator fun get(i: Int, j: Int): Point {
            require(isValidIdx(i, j)) { "($i, $j)" }
            return values[idxToOrdinal(i, j)]
        }

        /**
         * "a1" 形式で指定された位置の Point インスタンスを返します。
         * @throws IllegalArgumentException posが不正な形式または範囲外の場合
         */
        operator fun get(pos: String): Point = map[pos] ?: throw IllegalArgumentException(pos)
    }

    // 実装MEMO:
    // i, j, pos は ordinal から都度計算して返すようにカスタムゲッターを定義しても良いのだが、
    // Point インスタンスはせいぜい64個しか生成されないため、
    // 記憶域よりも都度演算の手間を省くことを優先させることにした。
    // めっちゃ利用されるはずだし。

    /** 縦座標 */
    val i: Int = ordinal / WIDTH

    /** 横座標 */
    val j: Int = ordinal % WIDTH

    /** "a1"～"h8" 形式の座標 */
    val pos: String = "%c%d".format('a' + j, 1 + i)

    /** この点の指定された方向の次の点。次の点が無い場合は null */
    operator fun plus(direction: Direction): Point? {
        val ni: Int = i + direction.di
        val nj: Int = j + direction.dj
        // これは null を返す設計が妥当だと信じる。nullを完全排除すればよいというものではない
        return if (isValidIdx(ni, nj)) get(ni, nj) else null
    }

    override fun toString(): String = pos

    override fun compareTo(other: Point): Int = ordinal.compareTo(other.ordinal)
}

package xyz.hotchpotch.reversi

import org.junit.jupiter.api.Test
import kotlin.test.*

internal class PointTest {

    @Test
    fun testGet1() {
        assertFailsWith(IndexOutOfBoundsException::class) { Point[-1, 0] }
        assertFailsWith(IndexOutOfBoundsException::class) { Point[0, -1] }
        assertFailsWith(IndexOutOfBoundsException::class) { Point[Point.HEIGHT, Point.WIDTH - 1] }
        assertFailsWith(IndexOutOfBoundsException::class) { Point[Point.HEIGHT - 1, Point.WIDTH] }

        // assertNotFail 的なやつ無いのかな？
        assertTrue(Point[0, 0] is Point)
        assertTrue(Point[Point.HEIGHT - 1, Point.WIDTH - 1] is Point)
    }

    @Test
    fun testGet2() {
        assertFailsWith(IllegalArgumentException::class) { Point[""] }
        assertFailsWith(IllegalArgumentException::class) { Point["A1"] }
        assertFailsWith(IllegalArgumentException::class) { Point["a0"] }
        assertFailsWith(IllegalArgumentException::class) { Point["${'a' - 1}1"] }
        assertFailsWith(IllegalArgumentException::class) { Point["${'a' + Point.WIDTH}${'1' + Point.HEIGHT - 1}"] }
        assertFailsWith(IllegalArgumentException::class) { Point["${'a' + Point.WIDTH - 1}${'1' + Point.HEIGHT}"] }
        assertFailsWith(IllegalArgumentException::class) { Point["xxxx"] }

        // assertNotFail 的なやつ無いのかな？
        assertTrue(Point["a1"] is Point)
        assertTrue(Point["${'a' + Point.WIDTH - 1}${'1' + Point.HEIGHT - 1}"] is Point)
    }

    @Test
    fun testInstance() {
        for (i in 0 until Point.HEIGHT) {
            for (j in 0 until Point.WIDTH) {
                val point: Point = Point[i, j]

                // Point[i, j] と Point[pos] で同一インスタンスが取得できること
                val pos = "${'a' + j}${1 + i}"
                assertSame(point, Point[pos])

                // 一応、i, j プロパティのテスト
                assertEquals(i, point.i)
                assertEquals(j, point.j)
            }
        }

        // 逆パターンも一応テスト
        for (c in 'a' until 'a' + Point.WIDTH) {
            for (n in '1' until Point.HEIGHT.toChar()) {
                val pos = "$c$n"
                val point: Point = Point[pos]

                // Point[pos] と Point[i, j] で同一インスタンスが取得できること
                val i: Int = n - '1'
                val j: Int = c - 'a'
                assertSame(point, Point[i, j])

                // 一応、pos プロパティのテスト
                assertEquals(pos, point.pos)
            }
        }
    }

    @Test
    fun plus() {
        assertNull(Point[0, 0] + Direction.UPPER)
        assertNull(Point[0, 0] + Direction.UPPER_RIGHT)
        assertSame(Point[0, 1], Point[0, 0] + Direction.RIGHT)
        assertSame(Point[1, 1], Point[0, 0] + Direction.LOWER_RIGHT)
        assertSame(Point[1, 0], Point[0, 0] + Direction.LOWER)
        assertNull(Point[0, 0] + Direction.LOWER_LEFT)
        assertNull(Point[0, 0] + Direction.LEFT)
        assertNull(Point[0, 0] + Direction.UPPER_LEFT)

        assertSame(Point[3, 4], Point[4, 4] + Direction.UPPER)
        assertSame(Point[3, 5], Point[4, 4] + Direction.UPPER_RIGHT)
        assertSame(Point[4, 5], Point[4, 4] + Direction.RIGHT)
        assertSame(Point[5, 5], Point[4, 4] + Direction.LOWER_RIGHT)
        assertSame(Point[5, 4], Point[4, 4] + Direction.LOWER)
        assertSame(Point[5, 3], Point[4, 4] + Direction.LOWER_LEFT)
        assertSame(Point[4, 3], Point[4, 4] + Direction.LEFT)
        assertSame(Point[3, 3], Point[4, 4] + Direction.UPPER_LEFT)

        val iMax = Point.HEIGHT - 1
        val jMax = Point.WIDTH - 1
        assertSame(Point[iMax - 1, jMax], Point[iMax, jMax] + Direction.UPPER)
        assertNull(Point[iMax, jMax] + Direction.UPPER_RIGHT)
        assertNull(Point[iMax, jMax] + Direction.RIGHT)
        assertNull(Point[iMax, jMax] + Direction.LOWER_RIGHT)
        assertNull(Point[iMax, jMax] + Direction.LOWER)
        assertNull(Point[iMax, jMax] + Direction.LOWER_LEFT)
        assertSame(Point[iMax, jMax - 1], Point[iMax, jMax] + Direction.LEFT)
        assertSame(Point[iMax - 1, jMax - 1], Point[iMax, jMax] + Direction.UPPER_LEFT)
    }

    @Test
    fun minus() {
        Point.values.forEach { p ->
            Direction.values().forEach { d ->
                assertSame(p + d.opposite, p - d)
            }
        }
    }

    @Test
    fun testToString() {
        for (c in 'a' until 'a' + Point.WIDTH) {
            for (n in '1' until Point.HEIGHT.toChar()) {
                assertEquals("$c$n", Point["$c$n"].toString())
            }
        }
    }
}

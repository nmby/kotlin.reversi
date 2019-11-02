package xyz.hotchpotch.reversi

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

internal class PointTest {

    @Test
    fun testInstance() {
        // Point[0, 0] と Point["a1"] で同一インスタンスが取得できること
        (0 until Point.HEIGHT).forEach { i ->
            (0 until Point.WIDTH).forEach { j ->
                val point: Point = Point[i, j]
                assertSame(point, Point[point.pos])
            }
        }

        ('a' until 'a' + Point.WIDTH).forEach { c ->
            ('1' until Point.HEIGHT.toChar()).forEach { n ->
                val point: Point = Point["$c$n"]
                val i: Int = n - '1'
                val j: Int = c - 'a'
                assertSame(point, Point[i, j])
            }
        }
    }

    @Test
    fun plus() {
        assertNull(Point[0, 0] + Direction.UPPER)
        assertNull(Point[0, 0] + Direction.UPPER_RIGHT)
        assertSame(Point[0, 1],Point[0, 0] + Direction.RIGHT)
        assertSame(Point[1, 1],Point[0, 0] + Direction.LOWER_RIGHT)
        assertSame(Point[1, 0],Point[0, 0] + Direction.LOWER)
        assertNull(Point[0, 0] + Direction.LOWER_LEFT)
        assertNull(Point[0, 0] + Direction.LEFT)
        assertNull(Point[0, 0] + Direction.UPPER_LEFT)

        assertSame(Point[3, 4],Point[4, 4] + Direction.UPPER)
        assertSame(Point[3, 5],Point[4, 4] + Direction.UPPER_RIGHT)
        assertSame(Point[4, 5],Point[4, 4] + Direction.RIGHT)
        assertSame(Point[5, 5],Point[4, 4] + Direction.LOWER_RIGHT)
        assertSame(Point[5, 4],Point[4, 4] + Direction.LOWER)
        assertSame(Point[5, 3],Point[4, 4] + Direction.LOWER_LEFT)
        assertSame(Point[4, 3],Point[4, 4] + Direction.LEFT)
        assertSame(Point[3, 3],Point[4, 4] + Direction.UPPER_LEFT)

        val iMax = Point.HEIGHT - 1
        val jMax = Point.WIDTH - 1
        assertSame(Point[iMax - 1, jMax], Point[iMax, jMax] + Direction.UPPER)
        assertNull(Point[iMax, jMax] + Direction.UPPER_RIGHT)
        assertNull(Point[iMax, jMax] + Direction.RIGHT)
        assertNull(Point[iMax, jMax] + Direction.LOWER_RIGHT)
        assertNull(Point[iMax, jMax] + Direction.LOWER)
        assertNull(Point[iMax, jMax] + Direction.LOWER_LEFT)
        assertSame(Point[iMax, jMax - 1],Point[iMax, jMax] + Direction.LEFT)
        assertSame(Point[iMax - 1, jMax - 1], Point[iMax, jMax] + Direction.UPPER_LEFT)
    }

    @Test
    fun testToString() {
        ('a' until 'a' + Point.WIDTH).forEach { c ->
            ('1' until Point.HEIGHT.toChar()).forEach { n ->
                assertEquals("$c$n", Point["$c$n"].toString())
            }
        }
    }
}
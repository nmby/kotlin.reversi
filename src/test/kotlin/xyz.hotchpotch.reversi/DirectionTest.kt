package xyz.hotchpotch.reversi

import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

internal class DirectionTest {

    @Test
    fun getOpposite() {
        assertSame(Direction.LOWER, Direction.UPPER.opposite)
        assertSame(Direction.LOWER_LEFT, Direction.UPPER_RIGHT.opposite)
        assertSame(Direction.LEFT, Direction.RIGHT.opposite)
        assertSame(Direction.UPPER_LEFT, Direction.LOWER_RIGHT.opposite)
        assertSame(Direction.UPPER, Direction.LOWER.opposite)
        assertSame(Direction.UPPER_RIGHT, Direction.LOWER_LEFT.opposite)
        assertSame(Direction.RIGHT, Direction.LEFT.opposite)
        assertSame(Direction.LOWER_RIGHT, Direction.UPPER_LEFT.opposite)
    }

    @Test
    fun unaryMinus() {
        assertSame(Direction.LOWER, -Direction.UPPER)
        assertSame(Direction.LOWER_LEFT, -Direction.UPPER_RIGHT)
        assertSame(Direction.LEFT, -Direction.RIGHT)
        assertSame(Direction.UPPER_LEFT, -Direction.LOWER_RIGHT)
        assertSame(Direction.UPPER, -Direction.LOWER)
        assertSame(Direction.UPPER_RIGHT, -Direction.LOWER_LEFT)
        assertSame(Direction.RIGHT, -Direction.LEFT)
        assertSame(Direction.LOWER_RIGHT, -Direction.UPPER_LEFT)
    }
}
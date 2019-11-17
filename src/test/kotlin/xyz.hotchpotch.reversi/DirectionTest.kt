package xyz.hotchpotch.reversi

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

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
}
package xyz.hotchpotch.reversi

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class ColorTest {

    @Test
    fun reversed() {
        assertSame(Color.WHITE, Color.BLACK.reversed())
        assertSame(Color.BLACK, Color.WHITE.reversed())
    }

    @Test
    fun testToString() {
        assertEquals("●", Color.BLACK.toString())
        assertEquals("○", Color.WHITE.toString())
    }
}
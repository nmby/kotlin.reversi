package xyz.hotchpotch.reversi

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import xyz.hotchpotch.reversi.Color.*

internal class ColorTest {

    @Test
    fun reversed() {
        assertSame(WHITE, BLACK.reversed())
        assertSame(BLACK, WHITE.reversed())
    }

    @Test
    fun testToString() {
        assertEquals("●", with(BLACK) { toString() })
        assertEquals("○", with(WHITE) { toString() })
    }
}
package xyz.hotchpotch.reversi

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class MoveTest {

    @Test
    fun isPass() {
        // こういうのループで全パタンやった方が良いのかな？？
        assertFalse(Move(Color.BLACK, Point["a1"]).isPass())
        assertFalse(Move(Color.WHITE, Point["h8"]).isPass())

        assertTrue(Move(Color.BLACK, null).isPass())
        assertTrue(Move(Color.WHITE, null).isPass())
    }

    @Test
    fun testToString() {
        assertEquals("●: a1", Move(Color.BLACK, Point[0, 0]).toString())
        assertEquals("○: b1", Move(Color.WHITE, Point[0, 1]).toString())

        assertEquals("●: g8", Move(Color.BLACK, Point[7, 6]).toString())
        assertEquals("○: h8", Move(Color.WHITE, Point[7, 7]).toString())
    }
}
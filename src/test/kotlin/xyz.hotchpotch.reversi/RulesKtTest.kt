package xyz.hotchpotch.reversi

import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class RulesKtTest {

    // こういうテスト、どう設計した良いのか分からん・・・
    // カバレッジ見ながらスマートに設計できるもんなんだろうか・・・
    private val map: Map<Point, Color> = mapOf(
            Point["a1"] to Color.WHITE,
            Point["a2"] to Color.WHITE,
            Point["a3"] to Color.WHITE,
            Point["a4"] to Color.WHITE,
            Point["a5"] to Color.WHITE,
            Point["a6"] to Color.WHITE,
            Point["a7"] to Color.WHITE,
            Point["b1"] to Color.WHITE,
            Point["b7"] to Color.WHITE,
            Point["c1"] to Color.WHITE,
            Point["c7"] to Color.WHITE,
            Point["d1"] to Color.WHITE,
            Point["d7"] to Color.WHITE,
            Point["e1"] to Color.WHITE,
            Point["e7"] to Color.WHITE,
            Point["f1"] to Color.WHITE,
            Point["f7"] to Color.WHITE,
            Point["g1"] to Color.WHITE,
            Point["g2"] to Color.WHITE,
            Point["g3"] to Color.WHITE,
            Point["g4"] to Color.WHITE,
            Point["g5"] to Color.WHITE,
            Point["g6"] to Color.WHITE,
            Point["g7"] to Color.WHITE,
            Point["b2"] to Color.BLACK,
            Point["b3"] to Color.BLACK,
            Point["b4"] to Color.BLACK,
            Point["b5"] to Color.BLACK,
            Point["b6"] to Color.BLACK,
            Point["c2"] to Color.BLACK,
            Point["c3"] to Color.BLACK,
            Point["c4"] to Color.BLACK,
            Point["c5"] to Color.BLACK,
            Point["c6"] to Color.BLACK,
            Point["d2"] to Color.BLACK,
            Point["d3"] to Color.BLACK,
            Point["d5"] to Color.BLACK,
            Point["d6"] to Color.BLACK,
            Point["e2"] to Color.BLACK,
            Point["e3"] to Color.BLACK,
            Point["e4"] to Color.BLACK,
            Point["e5"] to Color.BLACK,
            Point["e6"] to Color.BLACK,
            Point["f2"] to Color.BLACK,
            Point["f3"] to Color.BLACK,
            Point["f4"] to Color.BLACK,
            Point["f5"] to Color.BLACK,
            Point["f6"] to Color.BLACK
    )
    private val board: Board = boardOf(map)

    @Test
    fun reversibles() {
        assert(board.toString() == ("" +
                "  a b c d e f g h %n" +
                "1 ○ ○ ○ ○ ○ ○ ○ ・ %n" +
                "2 ○ ● ● ● ● ● ○ ・ %n" +
                "3 ○ ● ● ● ● ● ○ ・ %n" +
                "4 ○ ● ● ・ ● ● ○ ・ %n" +
                "5 ○ ● ● ● ● ● ○ ・ %n" +
                "6 ○ ● ● ● ● ● ○ ・ %n" +
                "7 ○ ○ ○ ○ ○ ○ ○ ・ %n" +
                "8 ・ ・ ・ ・ ・ ・ ・ ・ %n").format())

        Point.values.forEach {
            // 黒のテスト
            when (it.pos) {
                "a8" -> assertEquals(setOf(Point["b7"]), board.reversibles(Color.BLACK, it))
                "b8" -> assertEquals(setOf(Point["b7"], Point["c7"]), board.reversibles(Color.BLACK, it))
                "c8" -> assertEquals(setOf(Point["c7"], Point["d7"]), board.reversibles(Color.BLACK, it))
                "d8" -> assertEquals(setOf(Point["c7"], Point["d7"], Point["e7"]), board.reversibles(Color.BLACK, it))
                "e8" -> assertEquals(setOf(Point["d7"], Point["e7"]), board.reversibles(Color.BLACK, it))
                "f8" -> assertEquals(setOf(Point["e7"], Point["f7"]), board.reversibles(Color.BLACK, it))
                "g8" -> assertEquals(setOf(Point["f7"]), board.reversibles(Color.BLACK, it))
                "h1" -> assertEquals(setOf(Point["g2"]), board.reversibles(Color.BLACK, it))
                "h2" -> assertEquals(setOf(Point["g2"], Point["g3"]), board.reversibles(Color.BLACK, it))
                "h3" -> assertEquals(setOf(Point["g3"], Point["g4"]), board.reversibles(Color.BLACK, it))
                "h4" -> assertEquals(setOf(Point["g3"], Point["g4"], Point["g5"]), board.reversibles(Color.BLACK, it))
                "h5" -> assertEquals(setOf(Point["g4"], Point["g5"]), board.reversibles(Color.BLACK, it))
                "h6" -> assertEquals(setOf(Point["g5"], Point["g6"]), board.reversibles(Color.BLACK, it))
                "h7" -> assertEquals(setOf(Point["g6"]), board.reversibles(Color.BLACK, it))
                "h8" -> assertEquals(setOf(Point["g7"]), board.reversibles(Color.BLACK, it))
                else -> assertEquals(setOf(), board.reversibles(Color.BLACK, it))
            }

            // 白のテスト
            when (it.pos) {
                "d4" -> assertEquals(setOf(
                        Point["d3"], Point["d2"],
                        Point["e3"], Point["f2"],
                        Point["e4"], Point["f4"],
                        Point["e5"], Point["f6"],
                        Point["d5"], Point["d6"],
                        Point["c5"], Point["b6"],
                        Point["c4"], Point["b4"],
                        Point["c3"], Point["b2"]),
                        board.reversibles(Color.WHITE, it))
                else -> assertEquals(setOf(), board.reversibles(Color.WHITE, it))
            }
        }
    }

    @Test
    fun canPutAt() {
        Point.values.forEach {
            // 黒のテスト
            when (it.pos) {
                "a8", "b8", "c8", "d8", "e8", "f8", "g8",
                "h1", "h2", "h3", "h4", "h5", "h6", "h7", "h8"
                -> assertTrue(board.canPutAt(Color.BLACK, it))
                else -> assertFalse(board.canPutAt(Color.BLACK, it))
            }

            // 白のテスト
            when (it.pos) {
                "d4" -> assertTrue(board.canPutAt(Color.WHITE, it))
                else -> assertFalse(board.canPutAt(Color.WHITE, it))
            }
        }
    }

    @Test
    fun canPut() {
        assertTrue(board.canPut(Color.BLACK))
        assertTrue(board.canPut(Color.WHITE))

        assertFalse(boardOf(mapOf()).canPut(Color.BLACK))
        assertFalse(boardOf(mapOf()).canPut(Color.WHITE))
    }

    @Test
    fun canApply() {
        Point.values.forEach {
            // 黒のテスト
            when (it.pos) {
                "a8", "b8", "c8", "d8", "e8", "f8", "g8",
                "h1", "h2", "h3", "h4", "h5", "h6", "h7", "h8"
                -> assertTrue(board.canApply(Move(Color.BLACK, it)))
                else -> assertFalse(board.canApply(Move(Color.BLACK, it)))
            }

            // 白のテスト
            when (it.pos) {
                "d4" -> assertTrue(board.canApply(Move(Color.WHITE, it)))
                else -> assertFalse(board.canApply(Move(Color.WHITE, it)))
            }
        }
        assertFalse(board.canApply(Move(Color.BLACK, null)))
        assertFalse(board.canApply(Move(Color.WHITE, null)))

        // ゲーム終了状態の場合
        val empty: Board = boardOf(mapOf())
        Point.values.forEach {
            assertFalse(empty.canApply(Move(Color.BLACK, it)))
            assertFalse(empty.canApply(Move(Color.WHITE, it)))
        }
        assertFalse(empty.canApply(Move(Color.BLACK, null)))
        assertFalse(empty.canApply(Move(Color.WHITE, null)))

        // ゲーム継続中だがパスの場合
        val onlyBlackCan: Board = boardOf(mapOf(
                Point["a1"] to Color.BLACK,
                Point["b1"] to Color.WHITE))
        assertTrue(onlyBlackCan.canApply(Move(Color.WHITE, null)))
    }

    @Test
    fun isGameOngoing() {
        assertTrue(board.isGameOngoing())
        assertFalse(boardOf(mapOf()).isGameOngoing())
    }

    @Test
    fun winner() {
        assertThrows(IllegalStateException::class.java) { board.winner() }

        assertNull(boardOf(mapOf()).winner())
        assertSame(Color.BLACK, boardOf(mapOf(Point["a1"] to Color.BLACK)).winner())
        assertSame(Color.WHITE, boardOf(mapOf(Point["a1"] to Color.WHITE)).winner())
    }
}

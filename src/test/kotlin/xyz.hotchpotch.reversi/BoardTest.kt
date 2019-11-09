package xyz.hotchpotch.reversi

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals

internal class BoardTest {
    companion object {
        @JvmStatic
        private fun boards(): List<Board> = listOf(boardOf(), mutableBoardOf())
    }

    @ParameterizedTest
    @MethodSource("boards")
    fun initialBoards(board: Board) {

        // 生成直後の石の配置のテスト
        Point.values.forEach {
            when (it.pos) {
                "d4", "e5" -> assertSame(Color.WHITE, board[it])
                "d5", "e4" -> assertSame(Color.BLACK, board[it])
                else -> assertNull(board[it])
            }
        }

        // toString() のテスト
        assertEquals(
                (""
                        + "  a b c d e f g h %n"
                        + "1 ・・・・・・・・%n"
                        + "2 ・・・・・・・・%n"
                        + "3 ・・・・・・・・%n"
                        + "4 ・・・○●・・・%n"
                        + "5 ・・・●○・・・%n"
                        + "6 ・・・・・・・・%n"
                        + "7 ・・・・・・・・%n"
                        + "8 ・・・・・・・・%n").format(),
                board.toString()
        )

        assertEquals(2, board.count(Color.BLACK))
        assertEquals(2, board.count(Color.WHITE))
    }

    @Test
    fun anyMethods() {
        val board1a: Board = boardOf()
        val board2: MutableBoard = mutableBoardOf()

        assertTrue(board1a == board2)
        assertEquals(board1a.hashCode(), board2.hashCode())
        assertEquals(board1a.toString(), board2.toString())

        val move = Move(Color.BLACK, Point["c4"])
        val board1b: Board = board1a + move
        board2.apply(move)

        assertTrue(board1b == board2)
        assertEquals(board1b.hashCode(), board2.hashCode())
        assertEquals(board1b.toString(), board2.toString())

        assertFalse(board1b.equals("dummy"))
    }

    @ParameterizedTest
    @MethodSource("boards")
    fun plus(board: Board) {

        // 一手目：全パターン検証する。
        Point.values.forEach {
            when (it.pos) {
                "d3" -> assertEquals(
                        (""
                                + "  a b c d e f g h %n"
                                + "1 ・・・・・・・・%n"
                                + "2 ・・・・・・・・%n"
                                + "3 ・・・●・・・・%n"
                                + "4 ・・・●●・・・%n"
                                + "5 ・・・●○・・・%n"
                                + "6 ・・・・・・・・%n"
                                + "7 ・・・・・・・・%n"
                                + "8 ・・・・・・・・%n"
                                ).format(),
                        (board + Move(Color.BLACK, it)).toString()
                )
                "c4" -> assertEquals(
                        (""
                                + "  a b c d e f g h %n"
                                + "1 ・・・・・・・・%n"
                                + "2 ・・・・・・・・%n"
                                + "3 ・・・・・・・・%n"
                                + "4 ・・●●●・・・%n"
                                + "5 ・・・●○・・・%n"
                                + "6 ・・・・・・・・%n"
                                + "7 ・・・・・・・・%n"
                                + "8 ・・・・・・・・%n"
                                ).format(),
                        (board + Move(Color.BLACK, it)).toString()
                )
                "f5" -> assertEquals(
                        (""
                                + "  a b c d e f g h %n"
                                + "1 ・・・・・・・・%n"
                                + "2 ・・・・・・・・%n"
                                + "3 ・・・・・・・・%n"
                                + "4 ・・・○●・・・%n"
                                + "5 ・・・●●●・・%n"
                                + "6 ・・・・・・・・%n"
                                + "7 ・・・・・・・・%n"
                                + "8 ・・・・・・・・%n"
                                ).format(),
                        (board + Move(Color.BLACK, it)).toString()
                )
                "e6" -> assertEquals(
                        (""
                                + "  a b c d e f g h %n"
                                + "1 ・・・・・・・・%n"
                                + "2 ・・・・・・・・%n"
                                + "3 ・・・・・・・・%n"
                                + "4 ・・・○●・・・%n"
                                + "5 ・・・●●・・・%n"
                                + "6 ・・・・●・・・%n"
                                + "7 ・・・・・・・・%n"
                                + "8 ・・・・・・・・%n"
                                ).format(),
                        (board + Move(Color.BLACK, it)).toString()
                )
                else -> assertThrows(IllegalArgumentException::class.java)
                { assertNull(board + Move(Color.BLACK, it)) }
            }

            // 二手目は代表1パターンのみ
            assertEquals(
                    (""
                            + "  a b c d e f g h %n"
                            + "1 ・・・・・・・・%n"
                            + "2 ・・・・・・・・%n"
                            + "3 ・・○●・・・・%n"
                            + "4 ・・・○●・・・%n"
                            + "5 ・・・●○・・・%n"
                            + "6 ・・・・・・・・%n"
                            + "7 ・・・・・・・・%n"
                            + "8 ・・・・・・・・%n"
                            ).format(),
                    (board + Move(Color.BLACK, Point["d3"]) + Move(Color.WHITE, Point["c3"])).toString()
            )
        }
    }

    @Test
    fun apply() {
        val board: MutableBoard = mutableBoardOf()

        // お勉強MEMO: こういうテストを体系立てて実装するの難しい・・・

        // 異常系：置けない場所を指定した場合
        assertThrows(IllegalArgumentException::class.java) { board.apply(Move(Color.BLACK, Point["d4"])) }

        // 正常系：一手目
        board.apply(Move(Color.BLACK, Point["f5"]))
        assertEquals(
                (""
                        + "  a b c d e f g h %n"
                        + "1 ・・・・・・・・%n"
                        + "2 ・・・・・・・・%n"
                        + "3 ・・・・・・・・%n"
                        + "4 ・・・○●・・・%n"
                        + "5 ・・・●●●・・%n"
                        + "6 ・・・・・・・・%n"
                        + "7 ・・・・・・・・%n"
                        + "8 ・・・・・・・・%n"
                        ).format(),
                board.toString()
        )

        // 正常系：二手目
        board.apply(Move(Color.WHITE, Point["f4"]))
        assertEquals(
                (""
                        + "  a b c d e f g h %n"
                        + "1 ・・・・・・・・%n"
                        + "2 ・・・・・・・・%n"
                        + "3 ・・・・・・・・%n"
                        + "4 ・・・○○○・・%n"
                        + "5 ・・・●●●・・%n"
                        + "6 ・・・・・・・・%n"
                        + "7 ・・・・・・・・%n"
                        + "8 ・・・・・・・・%n"
                        ).format(),
                board.toString()
        )

        // 正常系：三手目
        board.apply(Move(Color.BLACK, Point["f3"]))
        assertEquals(
                (""
                        + "  a b c d e f g h %n"
                        + "1 ・・・・・・・・%n"
                        + "2 ・・・・・・・・%n"
                        + "3 ・・・・・●・・%n"
                        + "4 ・・・○●●・・%n"
                        + "5 ・・・●●●・・%n"
                        + "6 ・・・・・・・・%n"
                        + "7 ・・・・・・・・%n"
                        + "8 ・・・・・・・・%n"
                        ).format(),
                board.toString()
        )
    }

    @Test
    fun gameExit() {
        val board: MutableBoard = mutableBoardOf()

        // テストのため、ゲーム終了状態を作り出す。
        board.apply(Move(Color.BLACK, Point["d3"]))
        board.apply(Move(Color.BLACK, Point["e6"]))
        assert(
                board.toString() == (""
                        + "  a b c d e f g h %n"
                        + "1 ・・・・・・・・%n"
                        + "2 ・・・・・・・・%n"
                        + "3 ・・・●・・・・%n"
                        + "4 ・・・●●・・・%n"
                        + "5 ・・・●●・・・%n"
                        + "6 ・・・・●・・・%n"
                        + "7 ・・・・・・・・%n"
                        + "8 ・・・・・・・・%n").format()
        )

        // テスト
        val move = Move(Color.BLACK, Point["a1"])
        assertThrows(java.lang.IllegalArgumentException::class.java) { board.apply(move) }
        assertThrows(java.lang.IllegalArgumentException::class.java) { board + move }
        assertEquals(6, board.count(Color.BLACK))
        assertEquals(0, board.count(Color.WHITE))
    }

    @Test
    fun pass() {
        // 白にとってパスの状態のリバーシ盤。
        val board: MutableBoard =
                boardOf(mapOf(Point["a1"] to Color.BLACK, Point["b1"] to Color.WHITE)).toMutableBoard()
        assert(
                board.toString() == (""
                        + "  a b c d e f g h %n"
                        + "1 ●○・・・・・・%n"
                        + "2 ・・・・・・・・%n"
                        + "3 ・・・・・・・・%n"
                        + "4 ・・・・・・・・%n"
                        + "5 ・・・・・・・・%n"
                        + "6 ・・・・・・・・%n"
                        + "7 ・・・・・・・・%n"
                        + "8 ・・・・・・・・%n").format()
        )

        // テスト
        val move = Move(Color.WHITE, null)
        assertEquals(board, board + move)

        val copy: Board = boardOf(board)
        board.apply(move)
        assertEquals(copy, board)
    }

    @ParameterizedTest
    @MethodSource("boards")
    fun copy(board: Board) {
        val copy1: Board = board.toBoard()
        assertEquals(board, copy1)
        assertNotSame(board, copy1)

        val copy2: MutableBoard = board.toMutableBoard()
        assertEquals(board, copy2)
        assertNotSame(board, copy2)

        val copy3: Board = boardOf(board)
        assertEquals(board, copy3)
        assertNotSame(board, copy3)

        val copy4: MutableBoard = mutableBoardOf(board)
        assertEquals(board, copy4)
        assertNotSame(board, copy4)
    }

    @Test
    fun testWithOtherBoardClass() {
        val original: Board = boardOf()
        val other: Board = TestBoard(original)
        assertTrue(original == other)

        val original2: Board = boardOf(other)
        assertTrue(original2 == other)
    }
}

private class TestBoard(val proxy: Board) : Board by proxy

package xyz.hotchpotch.reversi.players

import xyz.hotchpotch.reversi.*
import xyz.hotchpotch.reversi.framework.Player
import xyz.hotchpotch.reversi.framework.PlayerFactory
import java.lang.Long.min
import java.time.Instant

/** 今回の手に費やせる時間を計算する際の余裕代 */
private const val MARGIN: Long = 20

class DepthFirstPlayer(private val color: Color, private val millisInTurn: Long) : Player {

    companion object : PlayerFactory {
        override fun create(color: Color, millisInGame: Long, millisInTurn: Long): Player =
                DepthFirstPlayer(color, millisInTurn)
    }

    private lateinit var deadline: Instant

    override fun choosePoint(board: Board, millisInGame: Long): Point? {
        val availables: List<Point> = Point.values.filter { board.canPutAt(color, it) }

        // 選べる手が0や1の場合は、探索を行わずに直ちに決定する。
        when (availables.size) {
            0 -> return null
            1 -> return availables[0]
        }

        deadline = deadline(board, millisInGame)
        val drawPoints: MutableList<Point> = mutableListOf()

        try {
            availables.forEach {

                // この候補手を選んだ場合の勝敗を、深さ優先探索で調べる。
                val winner: Color? = search(board + Move(color, it), color.reversed())

                // 勝てることが分かった場合はこの手に決定する。
                if (winner === color) return it

                // 引き分けに持ち込めることが分かったら、記録しておく。
                else if (winner === null) drawPoints.add(it)
            }

            // 引き分けに持ち込める手が見つかった場合はその手の中からランダムで、
            // 引き分けに持ち込める手が見つからなかった場合はすべての手の中からランダムで選択する。
            return if (drawPoints.isNotEmpty()) drawPoints.random() else availables.random()

        } catch (e: TimeUpException) {
            return availables.random()
        }
    }

    /** 今回の手に費やせる時間を計算し、探索を切り上げるデッドラインを返す。 */
    private fun deadline(board: Board, millisInGame: Long): Instant {
        val remainingMyTurns = (Point.values.filter { board[it] === null }.count() + 1) / 2
        val millisForThisTurn: Long = min(millisInTurn, millisInGame / remainingMyTurns) - MARGIN
        return Instant.now().plusMillis(millisForThisTurn)
    }

    /** ある手を選んだ場合の勝敗を深さ優先探索で調べ、今の段階での勝敗を返す。 */
    private fun search(board: Board, currColor: Color): Color? {
        if (!board.isGameOngoing()) return board.winner()

        // 時間切れの場合は探索を切り上げる。
        if (deadline < Instant.now()) throw TimeUpException()

        val availables: List<Point> = Point.values.filter { board.canPutAt(currColor, it) }

        // パスの場合は次に委ねる。
        if (availables.isEmpty()) return search(board, currColor.reversed())

        var canDraw = false
        availables.forEach {
            val winner: Color? = search(board + Move(currColor, it), currColor.reversed())

            // 勝てる手が一つでもある場合はその手を選べばよいため、勝ち確定。
            if (winner === currColor) return currColor

            // 引き分けに持ち込める場合があるか否かを記録しておく。
            else if (winner === null) canDraw = true
        }
        return if (canDraw) null else currColor.reversed()
    }
}

private class TimeUpException() : Exception()

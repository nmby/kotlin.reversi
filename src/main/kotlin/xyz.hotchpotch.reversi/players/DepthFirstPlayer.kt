package xyz.hotchpotch.reversi.players

import xyz.hotchpotch.reversi.*
import xyz.hotchpotch.reversi.framework.Player
import xyz.hotchpotch.reversi.framework.PlayerFactory
import java.lang.Long.min
import java.time.Instant

/** 今回の手に費やせる時間を計算する際の余裕代 */
private const val MARGIN: Long = 20

class DepthFirstPlayer(private val color: Color, private val millisAtTurn: Long) : Player {

    companion object : PlayerFactory {
        override fun create(color: Color, millisInGame: Long, millisAtTurn: Long): Player =
                DepthFirstPlayer(color, millisAtTurn)
    }

    private lateinit var deadline: Instant

    override fun choosePoint(board: Board, millisInGame: Long): Point? {
        val puttables: Set<Point> = board.puttables(color)

        // 選べる手が0や1の場合は、探索を行わずに直ちに決定する。
        when (puttables.size) {
            0 -> return null
            1 -> return puttables.first()
        }

        deadline = deadline(board, millisInGame)

        return try {
            search(board, color).first
        } catch (e: TimeUpException) {
            puttables.random()
        }
    }

    /** 今回の手に費やせる時間を計算し、探索を切り上げるデッドラインを返す。 */
    private fun deadline(board: Board, millisInGame: Long): Instant {
        val remainedMyTurns = (Point.values.filter { board[it] === null }.count() + 1) / 2
        val millisForThisTurn: Long = min(millisAtTurn, millisInGame / remainedMyTurns) - MARGIN
        return Instant.now().plusMillis(millisForThisTurn)
    }

    /**
     * 今後の推移を深さ優先探索で調べ、選択すべき手と、それによって得られる勝敗を返す。
     */
    // お勉強MEMO:
    // Color? を返す設計から Pair<Point?, Color?> を返す設計に変更。
    // 変更前の方がパフォーマンスは良い。変更した理由は以下の2点。
    //   - Pair と分割代入を実験的に使ってみたかったため
    //   - choosePoint 内にあったロジック重複を排除するため
    private fun search(board: Board, currColor: Color): Pair<Point?, Color?> {
        if (!board.isGameOngoing()) return null to board.winner()

        // 時間切れの場合は探索を切り上げる。
        if (deadline < Instant.now()) throw TimeUpException()

        val puttables: Set<Point> = board.puttables(currColor)

        // パスの場合は次に委ねる。
        if (puttables.isEmpty()) return null to search(board, currColor.reversed()).second

        val drawPoints: MutableSet<Point> = mutableSetOf()
        puttables.forEach {

            // お勉強MEMO:
            // 分割代入を半ば無理やり使ってみる。使いこなせば便利なのかも？？
            val (_, winner) = search(board + Move(currColor, it), currColor.reversed())

            // 勝てる手が一つでもある場合はその手を選べばよいため、勝ち確定。
            if (winner === currColor) return it to currColor

            // 引き分けに持ち込める場合は、その手を記録しておく。
            else if (winner === null) drawPoints.add(it)
        }
        return if (drawPoints.isNotEmpty()) drawPoints.random() to null
        else puttables.random() to currColor.reversed()
    }
}

private class TimeUpException() : Exception()

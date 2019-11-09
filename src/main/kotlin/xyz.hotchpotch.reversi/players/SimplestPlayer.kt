package xyz.hotchpotch.reversi.players

import xyz.hotchpotch.reversi.Board
import xyz.hotchpotch.reversi.Color
import xyz.hotchpotch.reversi.Point
import xyz.hotchpotch.reversi.canPutAt
import xyz.hotchpotch.reversi.framework.Player
import xyz.hotchpotch.reversi.framework.PlayerFactory

/**
 * リバーシ盤を左上から順に走査し、石を置ける場所が見つかったらそれを自身の手とするプレーヤーです。
 *
 * @param color このプレーヤーの石の色
 */
class SimplestPlayer(private val color: Color) : Player {
    companion object : PlayerFactory {
        override fun create(color: Color, millisInGame: Long, millisInTurn: Long): Player =
                SimplestPlayer(color)
    }

    /** リバーシ盤を左上から順に走査し、最初に見つかった置ける場所を自身の手として返します。 */
    override fun choosePoint(board: Board, millisInGame: Long): Point? =
            Point.values.firstOrNull { board.canPutAt(color, it) }
}

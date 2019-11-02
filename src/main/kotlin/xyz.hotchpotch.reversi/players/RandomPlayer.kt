package xyz.hotchpotch.reversi.players

import xyz.hotchpotch.reversi.Board
import xyz.hotchpotch.reversi.Color
import xyz.hotchpotch.reversi.Point
import xyz.hotchpotch.reversi.canPut
import xyz.hotchpotch.reversi.framework.Player
import xyz.hotchpotch.reversi.framework.PlayerFactory

class RandomPlayer(private val color: Color) : Player {
    companion object : PlayerFactory {
        override fun create(color: Color, millisInGame: Long, millisInTurn: Long): Player =
            RandomPlayer(color)
    }

    override fun choosePoint(board: Board, millisInGame: Long): Point? {
        val availables = Point.values().filter { board.canPut(color, it) }
        return if (availables.isEmpty()) null else availables.random()
    }
}

package xyz.hotchpotch.reversi.players

import xyz.hotchpotch.reversi.Board
import xyz.hotchpotch.reversi.Color
import xyz.hotchpotch.reversi.Point
import xyz.hotchpotch.reversi.canPutAt
import xyz.hotchpotch.reversi.framework.Player
import xyz.hotchpotch.reversi.framework.PlayerFactory

/**
 * 自身の手をランダムに選択するプレーヤーです。
 *
 * @param color このプレーヤーの石の色
 */
class RandomPlayer(private val color: Color) : Player {
    companion object : PlayerFactory {
        override fun create(color: Color, millisInGame: Long, millisInTurn: Long): Player =
                RandomPlayer(color)
    }

    /** 石を置ける位置の中からランダムで手を選んで返します。 */
    override fun choosePoint(board: Board, millisInGame: Long): Point? {
        val availables = Point.values().filter { board.canPutAt(color, it) }
        return if (availables.isEmpty()) null else availables.random()
    }
}

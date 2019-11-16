package xyz.hotchpotch.reversi.players

import xyz.hotchpotch.reversi.Board
import xyz.hotchpotch.reversi.Color
import xyz.hotchpotch.reversi.Point
import xyz.hotchpotch.reversi.framework.Player
import xyz.hotchpotch.reversi.framework.PlayerFactory
import xyz.hotchpotch.reversi.puttables

/**
 * 自身の手をランダムに選択するプレーヤーです。
 *
 * @param color このプレーヤーの石の色
 */
class RandomPlayer(private val color: Color) : Player {
    companion object : PlayerFactory {
        override fun create(color: Color, millisInGame: Long, millisAtTurn: Long): Player =
                RandomPlayer(color)
    }

    /** 石を置ける位置の中からランダムで手を選んで返します。 */
    override fun choosePoint(board: Board, millisInGame: Long): Point? {
        val puttables: Set<Point> = board.puttables(color)
        return if (puttables.isEmpty()) null else puttables.random()
    }
}

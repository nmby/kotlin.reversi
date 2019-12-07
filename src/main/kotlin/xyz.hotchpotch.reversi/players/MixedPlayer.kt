package xyz.hotchpotch.reversi.players

import xyz.hotchpotch.reversi.Board
import xyz.hotchpotch.reversi.Color
import xyz.hotchpotch.reversi.Point
import xyz.hotchpotch.reversi.framework.Player
import xyz.hotchpotch.reversi.framework.PlayerFactory

/**
 * ゲームの局面に応じて委託先Playerを切り替える混合プレーヤーです。
 *
 * @param color このプレーヤーの石の色
 * @param millisAtTurn 一手当たりの制限時間（ミリ秒）
 */
class MixedPlayer(color: Color, millisAtTurn: Long) : Player {

    companion object : PlayerFactory {
        override fun create(color: Color, millisInGame: Long, millisAtTurn: Long): Player =
                MixedPlayer(color, millisAtTurn)
    }

    private val randomPlayer: Player = RandomPlayer(color)
    private val ruleBasedPlayer: Player = RuleBasedPlayer(color)
    private val monteCarloPlayer: Player = MonteCarloPlayer(color, millisAtTurn)
    private val depthFirstPlayer: Player = DepthFirstPlayer(color, millisAtTurn)

    override fun choosePoint(board: Board, millisInGame: Long): Point? {
        val blankCells: Int = Point.values.filter { board[it] === null }.count()

        val player: Player = when {

            // 序盤はランダムに進めて持ち時間の温存を図る。
            50 < blankCells -> randomPlayer

            // 前半は経験則が活きるはず。
            35 < blankCells -> ruleBasedPlayer

            // 後半はモンテカルロ・シミュレーションの強みを活かす。
            10 < blankCells -> monteCarloPlayer

            // 残り10手くらいになれば全読みが可能なため、深さ優先探索による必勝手読みに託す。
            else -> depthFirstPlayer
        }
        return player.choosePoint(board, millisInGame)
    }
}

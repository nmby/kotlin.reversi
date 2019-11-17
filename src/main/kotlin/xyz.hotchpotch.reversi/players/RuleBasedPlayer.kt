package xyz.hotchpotch.reversi.players

import xyz.hotchpotch.reversi.*
import xyz.hotchpotch.reversi.framework.Player
import xyz.hotchpotch.reversi.framework.PlayerFactory

/**
 * 予めコーディングされた固有のロジックによって手を決定するプレーヤーです。
 *
 * @param color このプレーヤーの石の色
 */
class RuleBasedPlayer(private val color: Color) : Player {

    companion object : PlayerFactory {
        override fun create(color: Color, millisInGame: Long, millisAtTurn: Long): Player =
                RuleBasedPlayer(color)
    }

    override fun choosePoint(board: Board, millisInGame: Long): Point? {
        val puttables: Set<Point> = board.puttables(color)
        return when (puttables.size) {
            0 -> null
            1 -> puttables.first()
            else -> rule1PutCorner(puttables)
                    ?: rule2ExtendStableArea(board, puttables)
                    ?: rule3PutEffectivePoint(puttables)
                    ?: puttables.random()
        }
    }

    /** 角に置ける場合は、その角の位置を返します。 */
    private fun rule1PutCorner(puttables: Set<Point>): Point? = setOf(
            Point[0, 0],
            Point[Point.HEIGHT - 1, 0],
            Point[0, Point.WIDTH - 1],
            Point[Point.HEIGHT - 1, Point.WIDTH - 1])
            .filter { puttables.contains(it) }
            .let { if (it.isEmpty()) null else it.random() }

    /** 安定な石を増やせる手がある場合は、最も多く増やせる手を返します。 */
    private fun rule2ExtendStableArea(board: Board, puttables: Set<Point>): Point? {
        val countStables: (Board) -> Int =
                { b -> stablePoints(b).filter { p -> b[p] === color }.count() }
        val currStables: Int = countStables(board)
        return puttables
                .map { it to countStables(board + Move(color, it)) }
                .filter { currStables < it.second }
                .maxBy { it.second }?.first
    }

    /** 経験的に置くと有利になることが多い場所に置ける場合は、その手を返します。 */
    private fun rule3PutEffectivePoint(puttables: Set<Point>): Point? {

        // 角の2つ隣の点
        val effectives1: Set<Point> = setOf(
                Point[0, 2],
                Point[0, Point.WIDTH - 3],
                Point[2, 0],
                Point[2, Point.WIDTH - 1],
                Point[Point.HEIGHT - 3, 0],
                Point[Point.HEIGHT - 3, Point.WIDTH - 1],
                Point[Point.HEIGHT - 1, 2],
                Point[Point.HEIGHT - 1, Point.WIDTH - 3]
        )

        // 角から対角線上に2つ入った点
        val effectives2: Set<Point> = setOf(
                Point[2, 2],
                Point[2, Point.WIDTH - 3],
                Point[Point.HEIGHT - 3, 2],
                Point[Point.HEIGHT - 3, Point.WIDTH - 3]
        )

        // 中央4点に隣接する点
        val effectives3: Set<Point> = setOf(
                Point[2, 3],
                Point[2, Point.WIDTH - 4],
                Point[3, 2],
                Point[3, Point.WIDTH - 3],
                Point[Point.HEIGHT - 4, 2],
                Point[Point.HEIGHT - 4, Point.WIDTH - 3],
                Point[Point.HEIGHT - 3, 3],
                Point[Point.HEIGHT - 3, Point.WIDTH - 4]
        )

        val pickOneRandom: (Set<Point>) -> Point? = { ps ->
            ps.filter { puttables.contains(it) }
                    .let { if (it.isNotEmpty()) it.random() else null }
        }

        return pickOneRandom(effectives1)
                ?: pickOneRandom(effectives2)
                ?: pickOneRandom(effectives3)
    }
}

package xyz.hotchpotch.reversi.players

import xyz.hotchpotch.reversi.*
import xyz.hotchpotch.reversi.framework.Player
import xyz.hotchpotch.reversi.framework.PlayerFactory
import xyz.hotchpotch.reversi.framework.Record
import java.lang.Long.min
import java.time.Instant

/** 今回の手に費やせる時間を計算する際の余裕代 */
private const val MARGIN: Long = 20

/**
 * モンテカルロシミュレーションで最も勝率が高い手を選択するプレーヤーです。
 *
 * @param color このプレーヤーの石の色
 * @param millisInTurn 一手当たりの制限時間（ミリ秒）
 */
class MonteCarloPlayer(private val color: Color, private val millisInTurn: Long) : Player {

    companion object : PlayerFactory {
        override fun create(color: Color, millisInGame: Long, millisInTurn: Long): Player =
                MonteCarloPlayer(color, millisInTurn)
    }

    /**
     * 石の置ける位置を候補手としてモンテカルロシミュレーションを行い、
     * 最も勝率の高い手を選択して返します。
     */
    override fun choosePoint(board: Board, millisInGame: Long): Point? {

        // 石を置ける位置が0（パス）または1の場合は、直ちに手が決まる。
        val puttables = board.puttables(color)
        when (puttables.size) {
            0 -> return null
            1 -> return puttables[0]
        }

        val records: Map<Point, Record> = puttables.associateWith { Record() }

        // 時間がある限り、思考（試行）を繰り返す。
        val deadline: Instant = deadline(Instant.now(), board, millisInGame)
        while (Instant.now() < deadline) {

            puttables.forEach {
                val record: Record = records[it]!!
                when (playOut(mutableBoardOf(board + Move(color, it)), color.reversed())) {
                    color -> record.wins++
                    null -> record.draws++
                    else -> record.losses++
                }
            }
        }

        // 最も成績の良かった手を選択して返す。
        // max() を使うと同成績の位置が複数あったときに結果が偏る。
        // 特に一度もプレイアウトを行えなかった場合のことも考慮し、同率一位の中からランダムに選択することとする。
        val bestRecord: Record = records.map { it.value }.max()!!
        return records.filter { bestRecord <= it.value }.keys.random()
    }

    /** 今回の手に費やせる時間を計算し、最後の施行を始めるデッドラインを返す。 */
    private fun deadline(now: Instant, board: Board, millisInGame: Long): Instant {
        // このロジックは凝ろうと思えば色々と凝れるし、それで強くもなるが、
        // 面倒くさいので一旦これで
        val remainingMyTurns = (Point.values.filter { board[it] === null }.count() + 1) / 2
        val millisForThisTurn: Long = min(millisInTurn, millisInGame / remainingMyTurns) - MARGIN
        return now.plusMillis(millisForThisTurn)
    }

    /**
     * 指定されたリバーシ盤をランダムな手で更新し、末尾再帰的にゲーム終了まで進め、勝者の色を返します。
     *
     * @param board このプレイアウトのための変更可能リバーシ盤
     * @param currTurn 現在の手番の色
     * @return 勝者の色。引き分けの場合は null
     */
    // お勉強MEMO: せっかくなので末尾再帰（tailrec）ってやつを使ってみる。
    private tailrec fun playOut(board: MutableBoard, currTurn: Color): Color? {
        if (!board.isGameOngoing()) return board.winner()

        val puttables = board.puttables(currTurn)
        if (puttables.isNotEmpty()) board.apply(Move(currTurn, puttables.random()))
        return playOut(board, currTurn.reversed())
    }
}

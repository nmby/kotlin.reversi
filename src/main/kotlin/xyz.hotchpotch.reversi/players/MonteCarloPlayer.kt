package xyz.hotchpotch.reversi.players

import xyz.hotchpotch.reversi.*
import xyz.hotchpotch.reversi.framework.Player
import xyz.hotchpotch.reversi.framework.PlayerFactory
import java.lang.Long.min
import java.time.Instant
import java.util.stream.Stream

// 残り時間や処理性能とかに応じてここを可変にするとより良くなるんだろうけど
// 面倒なので今回はこれで。
private const val TIMES: Int = 30
private const val MARGIN: Long = 45

class MonteCarloPlayer(private val color: Color, private val millisInTurn: Long) : Player {

    private val proxy: Player = RandomPlayer.create(color, 0, 0)

    companion object : PlayerFactory {
        override fun create(color: Color, millisInGame: Long, millisInTurn: Long): Player =
                MonteCarloPlayer(color, millisInTurn)
    }

    override fun choosePoint(board: Board, millisInGame: Long): Point? {
        val now: Instant = Instant.now()

        val availables = Point.values().filter { board.canPut(color, it) }
        if (availables.isEmpty()) return null
        else if (availables.size == 1) return availables[0]

        val totalWins: MutableMap<Point, Long> = mutableMapOf()
        val deadline: Instant = deadline(now, board, millisInGame)
        while (Instant.now() < deadline) {
            val wins: Map<Point, Long> = availables.associateWith { tryOut(board, it) }
            availables.forEach { totalWins[it] = (totalWins[it] ?: 0) + (wins[it] ?: 0) }
        }

        // 時間がなく一度も施行できなかった場合はプロキシに委ねる。
        return if (totalWins.isEmpty()) proxy.choosePoint(board, millisInGame)
        // maxBy を使うと同成績の位置が複数あったときに結果が偏る気がするけど、まぁいいや
        else totalWins.maxBy { it.value }!!.key
    }

    private fun deadline(now: Instant, board: Board, millisInGame: Long): Instant {
        // このロジックは凝ろうと思えば色々と凝れるし、それで強くもなるが、
        // 面倒くさいので一旦これで
        val remainingMyTurns = (Point.values().filter { board[it] === null }.count() + 1) / 2
        val millisForThisTurn: Long = min(millisInTurn, millisInGame / remainingMyTurns) - MARGIN
        return now.plusMillis(millisForThisTurn)
    }

    /**
     * 現在のターンで自分が選択できる手の一つについて、トライアウトを規定回数実施し、
     * 自身の色が勝利した回数を返します。
     *
     * @param currBoard 現在のリバーシ盤
     * @param candidate 現在のターンで選択できる位置のうちの一つ
     * @param times トライアウトを行う回数
     * @return 自身の色が勝利した回数
     */
    // 本当は勝利回数だけじゃなくて引き分けの回数も考慮すると精度が上がるが、今回はまぁ良しとする。
    private fun tryOut(currBoard: Board, candidate: Point, times: Int = TIMES): Long {
        assert(currBoard.canPut(color, candidate))

        // ここで並列化するのが一番良いんじゃないかなー・・・　というのは根拠のない想定
        return Stream.generate { (currBoard + Move(color, candidate)).toMutableBoard() }
                .parallel()
                .limit(times.toLong())
                .map { tryOut(it) }
                .filter { it === color }
                .count()
    }

    /**
     * 指定されたリバーシ版に、相手のターンからゲーム終了までランダムに手を進めて行き、勝者の色を返します。
     *
     * @param board 今回の自分の手番を打ち終わった状態のリバーシ盤
     * @return ランダムな施行の結果の勝者の色
     */
    private fun tryOut(board: MutableBoard): Color? {
        var color = color.reversed()

        while (board.isGameOngoing()) {
            val availables = Point.values().filter { board.canPut(color, it) }
            if (availables.isNotEmpty()) board.apply(Move(color, availables.random()))
            color = color.reversed()
        }
        return board.winner()
    }
}
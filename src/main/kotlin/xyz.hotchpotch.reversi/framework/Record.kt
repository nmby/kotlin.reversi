package xyz.hotchpotch.reversi.framework

/**
 * あるプレーヤーの対戦成績（勝ち、引き分け、負けの数）を保持する可変クラスです。
 */
// お勉強MEMO:
// 本当は可変クラスは嫌いなのだが、まぁこれくらいなら良いかな？と思い可変にしちゃった。
// 可変クラスの何が嫌いかって、使うときに気をつけなきゃならないこと。
// 例えば、Mapのキーには使えないしSetにも入れるべきでないし、ソートした後に順序の妥当性が崩れるかもしれないし、etc...
data class Record(var wins: Int = 0, var draws: Int = 0, var losses: Int = 0) : Comparable<Record> {

    /** 総対戦回数（つまり勝ち、引き分け、負けの合計） */
    val totalPlays: Int
        get() = wins + losses + draws

    /** 勝ちの割合 */
    val winRatio: Double
        get() = wins.toDouble() / totalPlays

    /** 引き分けの割合 */
    val drawRatio: Double
        get() = draws.toDouble() / totalPlays

    /** 負けの割合 */
    val lossRatio: Double
        get() = losses.toDouble() / totalPlays

    /** 相手の対戦成績（つまりこの対戦成績の勝ち数と負け数を入れ替えたもの）を生成して返します。 */
    fun opposite(): Record = Record(wins = losses, draws = draws, losses = wins)

    /** この対戦成績に指定された対戦成績を加算します。 */
    fun add(other: Record) {
        wins += other.wins
        draws += other.draws
        losses += other.losses
    }

    /** この対戦成績と指定された対戦成績を合算したものを生成して返します。 */
    operator fun plus(other: Record): Record =
            Record(wins + other.wins, draws + other.draws, losses + other.losses)

    override fun compareTo(other: Record): Int = when {
        // 成績が良い方を「大きい」と評価する。
        winRatio != other.winRatio -> winRatio.compareTo(other.winRatio)
        lossRatio != other.lossRatio -> -lossRatio.compareTo(other.lossRatio)
        else -> 0
    }

    override fun toString(): String =
            "勝ち：${wins}(%.1f%%), 引き分け：${draws}(%.1f%%), 負け：${losses}(%.1f%%)"
                    .format(winRatio, drawRatio, lossRatio)
}

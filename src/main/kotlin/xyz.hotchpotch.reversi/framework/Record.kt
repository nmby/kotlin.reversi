package xyz.hotchpotch.reversi.framework

/**
 * あるプレーヤーの対戦回数（勝ち、負け、引き分け）を保持する可変クラスです。
 */
// 本当は可変クラスは嫌いなのだが、まぁこれくらいなら良いかな？と思い可変にしちゃった。
// 可変クラスの何が嫌いかって、使うときに気をつけなきゃならない。
// 例えば、Mapのキーには使えないしSetにも入れるべきでないし、ソートした後に順序の妥当性が崩れるかもしれないし、etc...
data class Record(var wins: Int = 0, var losses: Int = 0, var draws: Int = 0) : Comparable<Record> {

    /** 総対戦回数（つまり勝ち、負け、引き分けの合計）を返します。 */
    val totalPlays: Int
        get() = wins + losses + draws

    /** 勝ちの割合 */
    val winsRatio: Double
        get() = wins.toDouble() / totalPlays

    /** 負けの割合 */
    val lossesRatio: Double
        get() = losses.toDouble() / totalPlays

    /** 引き分けの割合 */
    val drawRatio: Double
        get() = draws.toDouble() / totalPlays

    /** 相手の対戦成績（つまりこの対戦成績の勝ち数と負け数を入れ替えたもの）を生成して返します。 */
    fun reversed(): Record = Record(wins = losses, losses = wins, draws = draws)

    operator fun plus(other: Record): Record =
            Record(wins + other.wins, losses + other.losses, draws + other.draws)

    override fun compareTo(other: Record): Int {
        // 成績が良い方を「大きい」と評価する。
        return when {
            winsRatio != other.winsRatio -> winsRatio.compareTo(other.winsRatio)
            lossesRatio != other.lossesRatio -> -lossesRatio.compareTo(other.lossesRatio)
            else -> 0
        }
    }

    override fun toString(): String =
            "勝ち：${wins}(%.1f%), 負け：${losses}(%.1f%), 引き分け：${draws}(%.1f%)"
                    .format(winsRatio, lossesRatio, drawRatio)
}

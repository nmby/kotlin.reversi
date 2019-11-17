package xyz.hotchpotch.reversi.framework

import xyz.hotchpotch.reversi.util.ConsoleScanner
import kotlin.reflect.KClass

/**
 * 複数のプレーヤーで総当たり戦を行う「リーグ」です。
 *
 * @param players 総当たり戦に参加するプレーヤー
 * @param millisInGame ゲーム内の各プレーヤーの持ち時間（ミリ秒）
 * @param millisAtTurn 一手当たりの制限時間（ミリ秒）
 * @param times 一組ごとの対戦回数
 * @param silent 標準出力への出力なしで進行させる場合は true。
 *               一マッチごとの状態をインタラクティブに出力しながら進行する場合は false
 */
class League(
        private val players: List<KClass<out Player>>,
        private val millisInGame: Long,
        private val millisAtTurn: Long,
        private val times: Int,
        private val silent: Boolean = false
) : Playable {

    companion object : PlayableFactory<League> {

        override val description: String = "複数プレーヤーで総当たり戦を行います。"

        override fun arrangeViaConsole(): League = League(
                arrangePlayers(),
                Scanners.millisInGame.get(),
                Scanners.millisAtTurn.get(),
                Scanners.times.get()
        )
    }

    /** プレーヤーごとの対戦成績 */
    private val records: MutableMap<Pair<KClass<out Player>, KClass<out Player>>, Record> = mutableMapOf()

    override fun play(): LeagueResult {
        check(records.values.all { it.totalPlays == 0 })
        { "Leagueクラスはいわゆるワンショットです。総当たり戦ごとに新たなインスタンスを利用してください。" }

        if (!silent) {
            println("\n総当たり戦を開始します。")
            players.forEachIndexed { idx, kClass ->
                println("\tプレーヤー${'A' + idx} ： ${kClass.qualifiedName}")
            }
            println("\tゲーム内の総持ち時間（ミリ秒） : $millisInGame")
            println("\t一手当たりの制限時間（ミリ秒） : $millisAtTurn")
            println("\t対戦回数 ： $times\n")
        }

        for (i in 0 until players.lastIndex) {
            for (j in i + 1..players.lastIndex) {
                val playerX: KClass<out Player> = players[i]
                val playerY: KClass<out Player> = players[j]

                if (!silent) print("${'A' + i} vs ${'A' + j} ...  ")

                val result: MatchResult = Match(
                        playerA = playerX,
                        playerB = playerY,
                        millisInGame = millisInGame,
                        millisAtTurn = millisAtTurn,
                        times = times,
                        automatic = true,
                        silent = true
                ).play()

                val xRecord: Record = result.record(playerX)

                if (!silent) {
                    println("${'A' + i}の勝ち：${xRecord.wins}, " +
                            "引き分け：${xRecord.draws}, " +
                            "${'A' + j}の勝ち：${xRecord.losses}")
                }

                records[playerX to playerY] = xRecord
                records[playerY to playerX] = xRecord.opposite()
            }
        }

        val result = LeagueResult(players, records)

        if (!silent) println(result.announce)

        return result
    }
}

class LeagueResult(
        players: List<KClass<out Player>>,
        records: Map<Pair<KClass<out Player>, KClass<out Player>>, Record>
) : Result {

    override val announce: String = {
        val grid: Array<Array<String>> =
                Array(players.size + 1) { Array(players.size + 2) { "" } }

        // 表のタイトル行の設定
        grid[0][0] = ""
        (0..players.lastIndex).forEach { grid[0][it + 1] = "[ vs ${'A' + it} ]" }
        grid[0][players.size + 1] = " [ TOTAL ]"

        // 表の一覧部分（各プレーヤーの成績部分）の設定
        for (i in 0..players.lastIndex) {
            val playerX: KClass<out Player> = players[i]
            val xTotalRecord = Record()
            grid[i + 1][0] = "[${'A' + i}] "

            for (j in 0..players.lastIndex) {
                if (i == j) {
                    grid[i + 1][j + 1] = "(-/-/-)"
                } else {
                    val playerY: KClass<out Player> = players[j]
                    val xRecord: Record = records[playerX to playerY] ?: throw AssertionError()
                    grid[i + 1][j + 1] = "(%d/%d/%d)".format(xRecord.wins, xRecord.draws, xRecord.losses)
                    xTotalRecord.add(xRecord)
                }
            }

            grid[i + 1][players.size + 1] = " %d(%.1f%%) / %d(%.1f%%) / %d(%.1f%%)".format(
                    xTotalRecord.wins, xTotalRecord.winRatio * 100,
                    xTotalRecord.draws, xTotalRecord.drawRatio * 100,
                    xTotalRecord.losses, xTotalRecord.lossRatio * 100)
        }

        // 各列の幅を調べる。
        val widths = IntArray(players.size + 2)
        for (j in 0..players.size + 1) {
            widths[j] = (0..players.size).map { grid[it][j].length }.max()!!
        }

        grid.joinToString(
                prefix = "\n総合成績（勝ち/分け/負け）：\n",
                separator = "\n",
                transform = {
                    it.mapIndexed { idx, s -> s.padEnd(widths[idx]) }
                            .joinToString(separator = "  ")
                })
    }()
}

/** 総当たり戦に参加させるプレーヤーを標準入出力を介してアレンジします。 */
private fun arrangePlayers(): List<KClass<out Player>> {
    val selected: MutableSet<Int> = mutableSetOf()

    while (true) {
        val n: Int = ConsoleScanner.forInt(
                startInclusive = 0,
                endInclusive = Player.aiPlayers.size,
                prompt = "\t0: (選択終了)\n" +
                        Player.aiPlayers.mapIndexed { idx, clz ->
                            "\t${if (selected.contains(idx)) "【選択済み】 " else ""}" +
                                    "${idx + 1}: ${clz.qualifiedName}\n"
                        }.joinToString("") +
                        "参加させるプレーヤーを番号で指定してください > "
        ).get()

        if (0 < n) {
            if (selected.contains(n - 1)) selected.remove(n - 1) else selected.add(n - 1)
        } else if (2 <= selected.size) {
            break
        } else {
            println("2つ以上のプレーヤーを選択してください。")
        }
    }
    return selected.sorted().map { Player.aiPlayers[it] }
}

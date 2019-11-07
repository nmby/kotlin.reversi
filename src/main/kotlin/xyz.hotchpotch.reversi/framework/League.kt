package xyz.hotchpotch.reversi.framework

import xyz.hotchpotch.reversi.util.ConsoleScanner
import kotlin.reflect.KClass

class League(
        private val players: List<KClass<out Player>>,
        private val millisInGame: Long,
        private val millisInTurn: Long,
        private val times: Int,
        private val silent: Boolean = false
) : Playable<Unit> {

    companion object : PlayableFactory<League> {
        override val description: String = "複数プレーヤーで総当たり戦を行います。"

        override fun arrangeViaConsole(): League = League(
                arrangePlayers(),
                Scanners.millisInGame.get(),
                Scanners.millisInTurn.get(),
                Scanners.times.get()
        )
    }

    /** プレーヤーごとの戦績 */
    private val records: MutableMap<Pair<KClass<out Player>, KClass<out Player>>, Record> = mutableMapOf()

    override fun play() {
        check(records.values.all { it.totalPlays == 0 })
        { "Leagueクラスはいわゆるワンショットです。総当たり戦ごとに新たなインスタンスを利用してください。" }

        if (!silent) {
            println("\n総当たり戦を開始します。")
            players.forEachIndexed { idx, kClass ->
                println("\tプレーヤー${'A' + idx} ： ${kClass.qualifiedName}")
            }
            println("\tゲーム内の総持ち時間（ミリ秒） : $millisInGame")
            println("\t一手当たりの制限時間（ミリ秒） : $millisInTurn")
            println("\t対戦回数 ： $times\n")
        }

        (0 until players.lastIndex).forEach { i ->
            (i + 1..players.lastIndex).forEach { j ->
                val playerX: KClass<out Player> = players[i]
                val playerY: KClass<out Player> = players[j]

                if (!silent) print("${'A' + i} vs ${'A' + j} ...  ")

                val match = Match(
                        playerA = playerX,
                        playerB = playerY,
                        millisInGame = millisInGame,
                        millisInTurn = millisInTurn,
                        times = times,
                        automatic = true,
                        silent = true
                )
                val result: Map<KClass<out Player>, Record> = match.play()
                val xRecord: Record = result[playerX]!!

                if (!silent) {
                    println("${'A' + i}の勝ち：${xRecord.wins}, " +
                            "引き分け：${xRecord.draws}, " +
                            "${'A' + j}の勝ち：${xRecord.losses}")
                }

                records[playerX to playerY] = xRecord
                records[playerY to playerX] = xRecord.reversed()
            }
        }

        if (!silent) {
            println("\n総合成績（勝ち/分け/負け）：")
            print((0..players.lastIndex).joinToString("") { "\t[ 対 ${'A' + it} ]" })
            println("\t[ TOTAL ]")

            val totalRecords: MutableMap<KClass<out Player>, Record> = mutableMapOf()
            val str: StringBuilder = StringBuilder()

            // MEMO: こういうのは無理にラムダにしなくて良いじゃん、手続き型ループで良いじゃん、 と思っている。
            (0..players.lastIndex).forEach { i ->
                val playerX: KClass<out Player> = players[i]
                val totalRecord = Record()
                str.append("[${'A' + i}]")

                (0..players.lastIndex).forEach { j ->
                    val playerY: KClass<out Player> = players[j]
                    if (i == j) {
                        str.append("\t(-/-/-)")
                    } else {
                        val record: Record = records[playerX to playerY]!!
                        str.append("\t(%d/%d/%d)".format(record.wins, record.draws, record.losses))
                        totalRecord.add(record)
                    }
                }

                str.append("\t%d(%.1f%%) / %d(%.1f%%) / %d(%.1f%%)\n".format(
                        totalRecord.wins, totalRecord.winsRatio * 100,
                        totalRecord.draws, totalRecord.drawRatio * 100,
                        totalRecord.losses, totalRecord.lossesRatio * 100))
                totalRecords[playerX] = totalRecord
            }
            print(str)
        }
    }
}

private fun arrangePlayers(): List<KClass<out Player>> {
    val selected: MutableSet<Int> = mutableSetOf()

    while (true) {
        val n: Int = ConsoleScanner.forInt(
                startInclusive = 0,
                endInclusive = Player.implementations.size,
                prompt = "\t0: (選択終了)\n" +
                        Player.implementations.mapIndexed { idx, clz ->
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
    return selected.sorted().map { Player.implementations[it] }
}
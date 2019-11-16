package xyz.hotchpotch.reversi.framework

import xyz.hotchpotch.reversi.Color
import xyz.hotchpotch.reversi.count
import kotlin.reflect.KClass

/**
 * 2プレーヤーで複数回対戦を行う「マッチ」です。
 *
 * @param playerA プレーヤーAのクラス
 * @param playerB プレーヤーBのクラス
 * @param millisInGame ゲーム内の各プレーヤーの持ち時間（ミリ秒）
 * @param millisAtTurn 一手当たりの制限時間（ミリ秒）
 * @param times 対戦回数
 * @param automatic 一ゲームごとに対話的に手を進める「対話モード」ではなく
 *                  自動でマッチ終了まで進める「自動モード」の場合は true
 * @param silent 標準出力への出力なしで進行させる場合は true。
 *               一ゲームごとの結果をインタラクティブに出力しながら進行する場合は false
 */
class Match(
        private val playerA: KClass<out Player>,
        private val playerB: KClass<out Player>,
        private val millisInGame: Long,
        private val millisAtTurn: Long,
        private val times: Int,
        private val automatic: Boolean = false,
        private val silent: Boolean = false
) : Playable {

    companion object : PlayableFactory<Match> {

        override val description: String = "2プレーヤーで黒白を入れ替えながら複数回対戦します。"

        override fun arrangeViaConsole(): Match = Match(
                Scanners.player("プレーヤーA", false).get(),
                Scanners.player("プレーヤーB", false).get(),
                Scanners.millisInGame.get(),
                Scanners.millisAtTurn.get(),
                Scanners.times.get(),
                Scanners.automatic.get()
        )
    }

    /** プレーヤーAの成績 */
    private val recordA = Record()

    /** プレーヤーAの色（一ゲームごとに切り替える。） */
    private var colorA: Color = Color.BLACK

    override fun play(): MatchResult {
        check(recordA.totalPlays == 0)
        { "Matchクラスはいわゆるワンショットです。対戦ごとに新たなインスタンスを利用してください。" }

        if (!silent) {
            print("\n対戦を開始します。\n")
            print("\tプレーヤーA : ${playerA.qualifiedName}\n" +
                    "\tプレーヤーB : ${playerB.qualifiedName}\n" +
                    "\tゲーム内の総持ち時間（ミリ秒） : $millisInGame\n" +
                    "\t一手当たりの制限時間（ミリ秒） : $millisAtTurn\n" +
                    "\t対戦回数 : $times\n" +
                    "> ")
            if (automatic) println() else readLine()
        }

        for (n in 1..times) {
            if (!silent) println("\n${n}回戦目（A=${colorA}, B=${colorA.reversed()}）\n")

            val gameResult: GameResult = Game(
                    playerBlack = if (colorA === Color.BLACK) playerA else playerB,
                    playerWhite = if (colorA === Color.BLACK) playerB else playerA,
                    millisInGame = millisInGame,
                    millisAtTurn = millisAtTurn,
                    automatic = true,
                    silent = true
            ).play()

            if (!silent) {
                println(gameResult.board.toString().prependIndent("\t"))
                print(when (gameResult.winner) {
                    colorA -> "Aの勝ちです。"
                    colorA.reversed() -> "Bの勝ちです。"
                    else -> "引き分けです。"
                })
                print("（A=${colorA}：${gameResult.board.count(colorA)}, " +
                        "B=${colorA.reversed()}:${gameResult.board.count(colorA.reversed())}） > ")
                if (automatic) println() else readLine()
            }
            when (gameResult.winner) {
                colorA -> recordA.wins++
                colorA.reversed() -> recordA.losses++
                else -> recordA.draws++
            }
            colorA = colorA.reversed()
        }

        val result = MatchResult(
                playerA to recordA,
                playerB to recordA.opposite())

        if (!silent) println(result.announce)

        return result
    }
}

class MatchResult(
        blackRecord: Pair<KClass<out Player>, Record>,
        whiteRecord: Pair<KClass<out Player>, Record>
) : Result {

    private val records: Map<KClass<out Player>, Record> = mapOf(blackRecord, whiteRecord)

    fun record(player: KClass<out Player>): Record = records[player]
            ?: throw IllegalArgumentException("このプレーヤーは参加していません: " + player.qualifiedName)

    override val announce: String = with(blackRecord.second) {
        "\n総合成績：\n\tAの勝ち：${wins}, 引き分け：${draws}, Bの勝ち：${losses}\n" +
                when {
                    losses < wins -> "\tAの勝ちです。"
                    wins < losses -> "\tBの勝ちです。"
                    else -> "\t引き分けです。"
                }
    }
}

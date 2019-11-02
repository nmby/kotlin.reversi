package xyz.hotchpotch.reversi.framework

import xyz.hotchpotch.reversi.*
import xyz.hotchpotch.reversi.util.ConsoleScanner
import java.time.Duration
import java.time.Instant
import kotlin.reflect.KClass

// このファイルはやっつけです。
// TODO: そのうちちゃんと作る
// @see https://github.com/nmby/reversi

fun main() {
    val isRepeat: ConsoleScanner<Boolean> = ConsoleScanner(
            judge = { true },
            converter = { it == "y" },
            prompt = "もう一度行いますか？ (y/N) > "
    )

    do {
        val condition = GameCondition.arrangeViaConsole()
        Game(condition).play()
    } while (isRepeat.get())
}

data class GameCondition(
        val playerBlack: KClass<out Player>,
        val playerWhite: KClass<out Player>,
        val millisInGame: Long,
        val millisInTurn: Long,
        val automatic: Boolean = true
) {

    companion object {
        fun arrangeViaConsole(): GameCondition {

            val playersList: String = players()
                    .mapIndexed { idx, playerClass -> "\t${idx + 1} : ${playerClass.qualifiedName}" }
                    .joinToString("\n")

            val playerBlack: KClass<out Player> = ConsoleScanner.forList(
                    list = players(),
                    prompt = "${playersList}\n${Color.BLACK} のプレーヤーを番号で選択してください > "
            ).get()

            val playerWhite: KClass<out Player> = ConsoleScanner.forList(
                    list = players(),
                    prompt = "${playersList}\n${Color.WHITE} のプレーヤーを番号で選択してください > "
            ).get()

            val minInGame: Long = 100
            val maxInGame: Long = 1000 * 60 * 60
            val millisInGame: Long = ConsoleScanner.forLong(
                    startInclusive = minInGame,
                    endInclusive = maxInGame,
                    prompt = "ゲーム内の持ち時間（ミリ秒）を ${minInGame}～${maxInGame} の範囲で指定してください > "
            ).get()

            val minInTurn: Long = 50
            val maxInTurn: Long = 1000 * 60 * 10
            val millisInTurn: Long = ConsoleScanner.forLong(
                    startInclusive = minInTurn,
                    endInclusive = maxInTurn,
                    prompt = "一手ごとの制限時間（ミリ秒）を ${minInTurn}～${maxInTurn} の範囲で指定してください > "
            ).get()

            val automatic: Boolean = ConsoleScanner(
                    judge = { it.toLowerCase() == "y" || it.toLowerCase() == "n" },
                    converter = { it == "y" },
                    prompt = "自動モードで実行しますか？ (y/N) > "
            ).get()

            return GameCondition(playerBlack, playerWhite, millisInGame, millisInTurn, automatic)
        }
    }

    override fun toString(): String = ("" +
            "${Color.BLACK} : ${playerBlack.qualifiedName}\n" +
            "${Color.WHITE} : ${playerWhite.qualifiedName}\n" +
            "ゲーム内の総持ち時間（ミリ秒） : $millisInGame\n" +
            "一手当たりの制限時間（ミリ秒） : $millisInTurn")
}

class Game(private val condition: GameCondition) {
    private val playerBlack: Player =
            createPlayer(condition.playerBlack, Color.BLACK, condition.millisInGame, condition.millisInTurn)
    private val playerWhite: Player =
            createPlayer(condition.playerWhite, Color.WHITE, condition.millisInGame, condition.millisInTurn)

    private fun player(color: Color): Player = if (color === Color.BLACK) playerBlack else playerWhite

    private var remainingMillisBlack: Long = condition.millisInGame
    private var remainingMillisWhite: Long = condition.millisInGame

    private val board: MutableBoard = mutableBoardOf()
    private var currTurn: Color = Color.BLACK

    fun play(): Color? {
        check(board.isGameOngoing()) { "Gameクラスはいわゆるワンショットです。ゲームごとに新たなインスタンスを利用してください。" }

        println("ゲームを開始します。")
        println(condition)

        while (board.isGameOngoing()) {
            println()
            println(board)
            print("$currTurn の番です... ")

            val before: Instant = Instant.now()

            val remainingMillis: Long =
                    if (currTurn === Color.BLACK) remainingMillisBlack else remainingMillisWhite
            val chosen: Point?
            try {
                // TODO: 別スレッドタスク化を検討
                chosen = player(currTurn).choosePoint(board.toBoard(), remainingMillis)

            } catch (e: Exception) {
                println("$currTurn の思考中に例外が発生しました。 $currTurn の負けです。")
                e.printStackTrace()
                return currTurn.reversed()
            }

            val passedMillis: Long = Duration.between(before, Instant.now()).toMillis()

            print("${chosen ?: "PASS"} が選択されました。（経過時間 : ${passedMillis}ミリ秒）")
            if (condition.automatic) println() else readLine()

            if (condition.millisInTurn < passedMillis) {
                println("一手当たりの持ち時間（${condition.millisInTurn}ミリ秒）を超過しました。 $currTurn の負けです。")
                return currTurn.reversed()
            }
            if (remainingMillis < passedMillis) {
                println("ゲーム内の持ち時間を超過しました。 $currTurn の負けです。\n" +
                        "（持ち時間${condition.millisInGame}ミリ秒を${passedMillis - remainingMillis}ミリ秒超過）")
                return currTurn.reversed()
            }
            if (currTurn === Color.BLACK) remainingMillisBlack -= passedMillis
            else remainingMillisWhite -= passedMillis

            val move = Move(currTurn, chosen)
            if (!board.canApply(move)) {
                println("ルール違反の手が指定されました。 $currTurn の負けです。")
                return currTurn.reversed()
            }

            board.apply(move)
            currTurn = currTurn.reversed()
        }

        println()
        println(board)
        print("ゲームが終了しました。")
        val winner = board.winner()
        if (winner !== null) println("$winner の勝ちです。") else println("引き分けです。")
        return winner
    }
}
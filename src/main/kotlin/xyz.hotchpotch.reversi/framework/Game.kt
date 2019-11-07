package xyz.hotchpotch.reversi.framework

import xyz.hotchpotch.reversi.*
import java.time.Duration
import java.time.Instant
import kotlin.reflect.KClass

/**
 * 2プレーヤーで1回対戦を行う「ゲーム」です。
 *
 * @param playerBlack 黒プレーヤーのクラス
 * @param playerWhite 白プレーヤーのクラス
 * @param millisInGame ゲーム内の各プレーヤーの持ち時間（ミリ秒）
 * @param millisInTurn 一手当たりの制限時間（ミリ秒）
 * @param automatic 一手ごとに対話的に手を進める「対話モード」ではなく
 *                  自動でゲーム完了まで進める「自動モード」の場合は true
 * @param silent 標準出力への出力なしで進行させる場合は true
 *               一手ごとの状態をインタラクティブに出力しながら進行する場合は false
 */
class Game(
        private val playerBlack: KClass<out Player>,
        private val playerWhite: KClass<out Player>,
        private val millisInGame: Long,
        private val millisInTurn: Long,
        private val automatic: Boolean = false,
        private val silent: Boolean = false
) : Playable<Color?> {

    companion object : PlayableFactory<Game> {
        override val description: String = "2プレーヤーで1回対戦します。"
        override fun arrangeViaConsole(): Game = Game(
                Scanners.player("${Color.BLACK} のプレーヤー").get(),
                Scanners.player("${Color.WHITE} のプレーヤー").get(),
                Scanners.millisInGame.get(),
                Scanners.millisInTurn.get()
        )
    }

    /** 黒・白プレーヤーごとのプロパティを表します。 */
    private interface Props {

        /** [Player] インスタンス */
        val player: Player

        /** ゲーム内での残り持ち時間（ミリ秒） */
        var remainedMillis: Long
    }

    /** 黒プレーヤーのプロパティ */
    private val blackProps = object : Props {
        override val player =
                createPlayer(playerBlack, Color.BLACK, millisInGame, millisInTurn)
        override var remainedMillis = millisInGame
    }

    /** 白プレーヤーのプロパティ */
    private val whiteProps = object : Props {
        override val player =
                createPlayer(playerWhite, Color.WHITE, millisInGame, millisInTurn)
        override var remainedMillis = millisInGame
    }

    private val board: MutableBoard = mutableBoardOf()
    private var currTurn: Color = Color.BLACK

    val resultBoard: Board
        // TODO: ステータスチェックする！
        get() = board.toBoard()

    override fun play(): Color? {
        check(board.isGameOngoing())
        { "Gameクラスはいわゆるワンショットです。ゲームごとに新たなインスタンスを利用してください。" }

        if (!silent) {
            println("\nゲームを開始します。")
            println("\t${Color.BLACK} : ${playerBlack.qualifiedName}\n" +
                    "\t${Color.WHITE} : ${playerWhite.qualifiedName}\n" +
                    "\tゲーム内の総持ち時間（ミリ秒） : $millisInGame\n" +
                    "\t一手当たりの制限時間（ミリ秒） : $millisInTurn\n" +
                    "> ")
            if (automatic) println() else readLine()
        }

        while (board.isGameOngoing()) {
            if (!silent) {
                println(board)
                print("$currTurn の番です...  ")
            }

            // あんまり広すぎる with は良くない。が、今回は実験ということで・・・
            // with の中身をコンパイル時ではなく実行時に指定できるのは便利そう。
            with(if (currTurn === Color.BLACK) blackProps else whiteProps) {

                val chosen: Point?
                val before: Instant = Instant.now()
                try {
                    chosen = player.choosePoint(board.toBoard(), remainedMillis)
                } catch (e: Exception) {
                    if (!silent) {
                        println("$currTurn の思考中に例外が発生しました。 $currTurn の負けです。")
                        e.printStackTrace()
                    }
                    return currTurn.reversed()
                }
                val passedMillis: Long = Duration.between(before, Instant.now()).toMillis()

                if (!silent) {
                    print("${chosen ?: "PASS"} が選択されました。（経過時間 : ${passedMillis}ミリ秒） > ")
                    if (automatic) println() else readLine()
                    println()
                }

                if (millisInTurn < passedMillis) {
                    if (!silent) println("一手当たりの持ち時間（${millisInTurn}ミリ秒）を" +
                            "${passedMillis - millisInTurn}ミリ秒超過しました。 $currTurn の負けです。")
                    return currTurn.reversed()
                }

                if (remainedMillis < passedMillis) {
                    if (!silent) println("ゲーム内の持ち時間（${millisInGame}ミリ秒）を" +
                            "${passedMillis - remainedMillis}ミリ秒超過しました。 $currTurn の負けです。")
                    return currTurn.reversed()
                }
                remainedMillis -= passedMillis

                val move = Move(currTurn, chosen)
                if (!board.canApply(move)) {
                    if (!silent) println("ルール違反の手が指定されました。 $currTurn の負けです。")
                    return currTurn.reversed()
                }
                board.apply(move)
            }
            currTurn = currTurn.reversed()
        }

        val winner = board.winner()
        if (!silent) {
            println(board)
            print("ゲームが終了しました。")
            if (winner === null) println("引き分けです。")
            else println(" $winner の勝ちです。 " +
                    "${Color.BLACK}:${board.count(Color.BLACK)}（残り${blackProps.remainedMillis}ミリ秒）, " +
                    "${Color.WHITE}:${board.count(Color.WHITE)}（残り${whiteProps.remainedMillis}ミリ秒）")
        }
        return winner
    }
}
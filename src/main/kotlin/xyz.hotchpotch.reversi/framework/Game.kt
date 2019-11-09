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
 * @param silent 標準出力への出力なしで進行させる場合は true。
 *               一手ごとの状態をインタラクティブに出力しながら進行する場合は false
 */
class Game(
        private val playerBlack: KClass<out Player>,
        private val playerWhite: KClass<out Player>,
        private val millisInGame: Long,
        private val millisInTurn: Long,
        private val automatic: Boolean = false,
        private val silent: Boolean = false
) : Playable<GameResult> {

    companion object : PlayableFactory<Game> {

        override val description: String = "2プレーヤーで1回対戦します。"

        override fun arrangeViaConsole(): Game = Game(
                Scanners.player("${Color.BLACK} のプレーヤー", true).get(),
                Scanners.player("${Color.WHITE} のプレーヤー", true).get(),
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

    override fun play(): GameResult {
        check(board.count(Color.BLACK) + board.count(Color.WHITE) == 4)
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
                    val result: GameResult = GameResult.HeHasGoneMad(
                            violator = currTurn,
                            cause = e,
                            board = board)
                    if (!silent) println(result.announce)
                    return result
                }

                val passedMillis: Long = Duration.between(before, Instant.now()).toMillis()

                if (!silent) {
                    print("${chosen ?: "PASS"} が選択されました。（経過時間 : ${passedMillis}ミリ秒） > ")
                    if (automatic) println() else readLine()
                    println()
                }

                // 一手当たりの制限時間を超過した場合
                if (millisInTurn < passedMillis) {
                    val result: GameResult = GameResult.OverTheTimeLimitInTurn(
                            violator = currTurn,
                            limit = millisInTurn,
                            exceeded = passedMillis - millisInTurn,
                            board = board
                    )
                    if (!silent) println(result.announce)
                    return result
                }

                // ゲーム内の持ち時間を超過した場合
                if (remainedMillis < passedMillis) {
                    val result: GameResult = GameResult.OverTheTimeLimitInGame(
                            violator = currTurn,
                            limit = millisInGame,
                            exceeded = passedMillis - remainedMillis,
                            board = board
                    )
                    if (!silent) println(result.announce)
                    return result
                }
                remainedMillis -= passedMillis

                val move = Move(currTurn, chosen)

                // ルール違反の手が指定された場合
                if (!board.canApply(move)) {
                    val result: GameResult = GameResult.SettlementWithIllegalMove(
                            violator = currTurn, board = board)
                    if (!silent) println(result.announce)
                    return result
                }

                board.apply(move)
            }
            currTurn = currTurn.reversed()
        }

        // 石を置ける場所が無くなりゲーム終了に至った場合
        val result: GameResult = GameResult.NormalSettlement(
                board = board,
                remainedMillisBlack = blackProps.remainedMillis,
                remainedMillisWhite = whiteProps.remainedMillis)

        if (!silent) println(result.announce)

        return result
    }
}

/**
 * ゲーム結果を表します。
 *
 * @param winner 勝者の石の色。引き分けの場合は null
 * @param resultBoard ゲーム終了時点のリバーシ盤
 */
// お勉強MEMO: 実験として sealed class を使ってみる。
sealed class GameResult(val winner: Color?, val resultBoard: Board) {
    abstract val announce: String

    /**
     * 通常のゲーム終了（双方石を置ける場所がなくなったこと）により決着が付いた場合のゲーム結果を表します。
     *
     * @param board ゲーム終了時点のリバーシ盤
     * @param remainedMillisBlack 黒プレーヤーの残り持ち時間（ミリ秒）
     * @param remainedMillisWhite 白プレーヤーの残り持ち時間（ミリ秒）
     */
    class NormalSettlement(board: Board, remainedMillisBlack: Long, remainedMillisWhite: Long)
        : GameResult(winner = board.winner(), resultBoard = board.toBoard()) {

        override val announce: String = {
            val str: StringBuilder = StringBuilder()
                    .appendln(board)
                    .append("ゲームが終了しました。")

            if (winner === null) str.appendln("引き分けです。")
            else str.append(" $winner の勝ちです。 " +
                    "${Color.BLACK}:${board.count(Color.BLACK)}（残り${remainedMillisBlack}ミリ秒）, " +
                    "${Color.WHITE}:${board.count(Color.WHITE)}（残り${remainedMillisWhite}ミリ秒）")

            str.toString()
        }()
    }

    /**
     * ルール違反の手が指定されたことにより決着が付いた場合のゲーム結果を表します。
     *
     * @param violator ルール違反の手を指したプレーヤーの石の色
     * @param board ゲーム終了時点のリバーシ盤
     */
    class SettlementWithIllegalMove(violator: Color, board: Board)
        : GameResult(winner = violator.reversed(), resultBoard = board.toBoard()) {

        override val announce: String = "ルール違反の手が指定されました。 $violator の負けです。"
    }

    /**
     * ゲーム内の持ち時間を超過したことにより決着が付いた場合のゲーム結果を表します。
     *
     * @param violator 時間超過したプレーヤーの石の色
     * @param limit 元々の持ち時間（ミリ秒）
     * @param exceeded 超過した時間（ミリ秒）
     * @param board ゲーム終了時点のリバーシ盤
     */
    class OverTheTimeLimitInGame(violator: Color, limit: Long, exceeded: Long, board: Board)
        : GameResult(winner = violator.reversed(), resultBoard = board.toBoard()) {

        override val announce: String =
                "ゲーム内の持ち時間（${limit}ミリ秒）を${exceeded}ミリ秒超過しました。 $violator の負けです。"
    }

    /**
     * 一手当たりの制限時間を超過したことにより決着が付いた場合のゲーム結果を表します。
     *
     * @param violator 時間超過したプレーヤーの石の色
     * @param limit 元々の制限時間（ミリ秒）
     * @param exceeded 超過した時間（ミリ秒）
     * @param board ゲーム終了時点のリバーシ盤
     */
    class OverTheTimeLimitInTurn(violator: Color, limit: Long, exceeded: Long, board: Board)
        : GameResult(winner = violator.reversed(), resultBoard = board.toBoard()) {

        override val announce: String =
                "一手当たりの制限時間（${limit}ミリ秒）を${exceeded}ミリ秒超過しました。 $violator の負けです。"
    }

    /**
     * プレーヤーの思考中に例外が発生したことにより決着が付いた場合のゲーム結果を表します。
     *
     * @param violator 例外を発生させたプレーヤーの石の色
     * @param cause 発生した例外
     * @param board ゲーム終了時点のリバーシ盤
     */
    // TODO: もうちょっとマシな名前を考える
    class HeHasGoneMad(violator: Color, cause: Exception, board: Board)
        : GameResult(winner = violator.reversed(), resultBoard = board.toBoard()) {

        override val announce: String = "$violator の思考中に例外が発生しました。 $violator の負けです。\n" +
                cause.stackTrace.joinToString("\n") { toString() }
    }
}

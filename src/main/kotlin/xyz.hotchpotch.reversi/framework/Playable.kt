package xyz.hotchpotch.reversi.framework

import xyz.hotchpotch.reversi.util.ConsoleScanner
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance

// お勉強MEMO:
// 一つのファイルに複数の要素を詰め込むの、どう考えればよいんだろう。
// 今回は実験として色々モリモリにしてみた。
fun main() {
    val playables: List<KClass<out Playable<*>>> = listOf(Game::class, Match::class)

    val menuList: String = playables
            .mapIndexed { idx, clazz ->
                "\t${idx + 1} : ${clazz.simpleName}" +
                        " - ${(clazz.companionObjectInstance as PlayableFactory<*>).description}"
            }
            .joinToString("\n")

    val selectMenu: ConsoleScanner<KClass<out Playable<*>>> = ConsoleScanner.forList(
            list = playables,
            prompt = "${menuList}\n番号で選択してください > "
    )

    val isRepeat: ConsoleScanner<Boolean> = ConsoleScanner(
            judge = { true },
            converter = { it == "y" },
            prompt = "\nもう一度行いますか？ (y/N) > ")

    do {
        val factory: Any? = selectMenu.get().companionObjectInstance
        check(factory is PlayableFactory<*>)
        factory.arrangeViaConsole().play()
    } while (isRepeat.get())
}

/**
 * 「遊べるもの」（例えば、2プレーヤーが1回対戦する「ゲーム」や、複数のプレーヤーが総当たり戦を行う「リーグ」）を表します。
 *
 * @param T 結果の型
 */
interface Playable<out T> {

    /** この [Playable] を実行して結果を返します。 */
    fun play(): T
}

/**
 * [Playable] のファクトリを表します。
 *
 * @param T [Playable] 実装クラスの型
 */
interface PlayableFactory<out T : Playable<Any?>> {

    /** この [Playable] の説明 */
    val description: String

    /**
     * 標準入出力から実行条件を取得して [Playable] オブジェクトを生成するファクトリ
     */
    fun arrangeViaConsole(): T
}

/** [Playable] 実装クラスの実行条件を標準入力から取得するためのスキャナーを集めたものです。 */
object Scanners {

    private val playersList: String = players()
            .mapIndexed { idx, playerClass -> "\t${idx + 1} : ${playerClass.qualifiedName}" }
            .joinToString("\n")

    /**
     * プレーヤークラスを取得するためのスキャナー
     *
     * @param playerDesc 標準出力に表示するプレーヤーの呼称（"●プレーヤー" や "プレーヤーA" など）
     */
    fun player(playerDesc: String): ConsoleScanner<KClass<out Player>> = ConsoleScanner.forList(
            list = players(),
            prompt = "${playersList}\n${playerDesc}を番号で選択してください > "
    )

    private const val minInGame: Long = 100
    private const val maxInGame: Long = 1000 * 60 * 60

    /** ゲーム内の持ち時間（ミリ秒）を取得するためのスキャナー */
    val millisInGame: ConsoleScanner<Long> = ConsoleScanner.forLong(
            startInclusive = minInGame,
            endInclusive = maxInGame,
            prompt = "ゲーム内の持ち時間（ミリ秒）を ${minInGame}～${maxInGame} の範囲で指定してください > "
    )

    private const val minInTurn: Long = 50
    private const val maxInTurn: Long = 1000 * 60 * 10

    /** 一手当たりの持ち時間（ミリ秒）を取得するためのスキャナー */
    val millisInTurn: ConsoleScanner<Long> = ConsoleScanner.forLong(
            startInclusive = minInTurn,
            endInclusive = maxInTurn,
            prompt = "一手ごとの制限時間（ミリ秒）を ${minInTurn}～${maxInTurn} の範囲で指定してください > "
    )

    private const val minTimes: Int = 2
    private const val maxTimes: Int = 1000

    /** 対戦回数を取得するためのスキャナー */
    val times: ConsoleScanner<Int> = ConsoleScanner.forInt(
            startInclusive = minTimes,
            endInclusive = maxTimes,
            prompt = "対戦回数を ${minTimes}～${maxTimes} の範囲で指定してください > "
    )
}

package xyz.hotchpotch.reversi.framework

import xyz.hotchpotch.reversi.util.ConsoleScanner
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance

// お勉強MEMO:
// 一つのファイルに複数の要素を詰め込むの、どう考えればよいんだろう。
// 今回は実験として色々モリモリにしてみた。

fun main() {
    val menuList: String = Playable.types
            .mapIndexed { idx, clazz ->
                "\t${idx + 1} : ${clazz.simpleName}" +
                        " - ${(clazz.companionObjectInstance as PlayableFactory<*>).description}"
            }
            .joinToString("\n")

    val selectMenu: ConsoleScanner<KClass<out Playable<*>>> = ConsoleScanner.forList(
            list = Playable.types,
            prompt = "${menuList}\n番号で選択してください > "
    )

    val isRepeat: ConsoleScanner<Boolean> = ConsoleScanner.yesOrNo("\nもう一度行いますか？")

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

    companion object {

        /** 既知の [Playable] 実装クラスの一覧 */
        // Playable 実装クラスの一覧を取得しやすくするために
        // Playable をインタフェースではなく sealed クラスにするとよいのかも？？
        // と思って一度してみたものの、
        // 全実装クラスをここに記述しなくてはならず不細工になる割には旨味が少ないので、やめた。
        // 本当はリフレクションで実現できると良いのだろうけど、今回はこれで良しとする。
        val types: List<KClass<out Playable<*>>> = listOf(Game::class, Match::class, League::class)
            get() = field.toList()
    }

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

    /** 標準入出力から実行条件を取得して [Playable] オブジェクトを生成するファクトリ */
    fun arrangeViaConsole(): T
}

/** [Playable] 実装クラスの実行条件を標準入力から取得するためのスキャナーを集めたものです。 */
object Scanners {

    private fun playersList(includeManualPlayer: Boolean): String =
            (if (includeManualPlayer) Player.allPlayers else Player.aiPlayers)
                    .mapIndexed { idx, playerClass -> "\t${idx + 1} : ${playerClass.qualifiedName}" }
                    .joinToString("\n")

    /**
     * プレーヤークラスを取得するためのスキャナー
     *
     * @param playerDesc 標準出力に表示するプレーヤーの呼称（"●プレーヤー" や "プレーヤーA" など）
     * @param includeManualPlayer 手動プレーヤーも対象に含める場合は true
     */
    fun player(playerDesc: String, includeManualPlayer: Boolean): ConsoleScanner<KClass<out Player>> =
            ConsoleScanner.forList(
                    list = if (includeManualPlayer) Player.allPlayers else Player.aiPlayers,
                    prompt = "${playersList(includeManualPlayer)}\n${playerDesc}を番号で選択してください > "
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

    /** 自動実行モードか否かを取得するためのスキャナー */
    val automatic: ConsoleScanner<Boolean> = ConsoleScanner.yesOrNo(
            question = "自動実行モードにしますか？")
}

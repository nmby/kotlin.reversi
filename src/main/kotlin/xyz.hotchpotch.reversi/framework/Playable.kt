package xyz.hotchpotch.reversi.framework

import xyz.hotchpotch.reversi.util.ConsoleScanner
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance

// お勉強MEMO:
// 一つのファイルに複数の要素を詰め込むの、どう考えればよいんだろう。
// 今回は実験として色々モリモリにしてみた。

fun main() {
    val menuList: String = Playable.types
            .mapIndexed { idx, type ->
                val factory: PlayableFactory<*> = type.companionObjectInstance as PlayableFactory<*>
                "\t${idx + 1} : ${type.simpleName} - ${factory.description}"
            }
            .joinToString("\n")

    val selectMenu: ConsoleScanner<KClass<out Playable>> = ConsoleScanner.forList(
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
 */
interface Playable {

    companion object {

        /** 既知の [Playable] 実装クラスの一覧 */
        // Playable 実装クラスの一覧を取得しやすくするために
        // Playable をインタフェースではなく sealed クラスにするとよいのかも？？
        // と思って一度してみたものの、
        // 全実装クラスをここに記述しなくてはならず不細工になる割には旨味が少ないので、やめた。
        // 本当はリフレクションで実現できると良いのだろうけど、今回はこれで良しとする。
        val types: List<KClass<out Playable>> = listOf(Game::class, Match::class, League::class)
            get() = field.toList()
    }

    /** この [Playable] を実行して結果を返します。 */
    fun play(): Result
}

/**
 * [Playable] のファクトリを表します。
 *
 * @param T [Playable] 実装クラスの型
 */
interface PlayableFactory<out T : Playable> {

    /** この [Playable] の説明 */
    val description: String

    /** 標準入出力から実行条件を取得して [Playable] オブジェクトを生成するファクトリ */
    fun arrangeViaConsole(): T
}

/**
 * [Playable.play] の実行結果を表します。
 */
interface Result {

    /** 結果を表す文字列 */
    val announce: String
}

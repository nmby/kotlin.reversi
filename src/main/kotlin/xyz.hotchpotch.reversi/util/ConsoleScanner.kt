package xyz.hotchpotch.reversi.util

import java.util.regex.Pattern
import kotlin.reflect.KClass

private const val DEFAULT_PROMPT: String = "> "
private const val DEFAULT_CAUTION: String = "入力形式が不正です。"

/**
 * 標準入出力から対話的にユーザー入力値を取得するためのスキャナーです。
 * [ConsoleScanner] は求める形式の入力が得られるまでユーザーに何度も再入力を求め、
 * クライアントが必要とする型に変換したうえで返します。
 *
 * 例） 1～10の範囲の整数値を取得したい場合
 * クライアント側で次のように記述することで、目的の整数値を取得できます。
 * ```kotlin
 *     val input: Int = ConsoleScanner.forInt(1, 10).get()
 * ```
 *
 * 例）リストの中から１つの要素を選ばせたい場合
 * ```kotlin
 *     val chosen: T = ConsoleScanner.forList(list).get()
 * ```
 *
 * 上記は一例です。その他の機能については各メンバの説明を参照してください。
 *
 * @param T 最終的に取得したいオブジェクトの型
 * @param judge 標準入力から入力された文字列が要求する形式を満たしているかをチェックする関数
 * @param converter 標準入力から入力された文字列を目的の型のオブジェクトに変換する関数
 * @param prompt ユーザーに入力を促すために標準出力に表示する文字列
 * @param caution ユーザーが誤った形式の入力を行った場合にそれを知らせるために標準出力に表示する文字列
 */
class ConsoleScanner<out T>(
        private val judge: (String) -> Boolean,
        private val converter: (String) -> T,
        private val prompt: String = DEFAULT_PROMPT,
        private val caution: String = DEFAULT_CAUTION
) {

    companion object {

        /**
         * 指定された形式の文字列を取得するスキャナーを生成します。
         *
         * @param pattern 取得する文字列の形式
         * @param prompt ユーザーに入力を促す文字列
         * @param caution ユーザーが入力を誤ったときにそれを知らせる文字列
         */
        fun forString(
                pattern: Pattern,
                prompt: String = DEFAULT_PROMPT,
                caution: String = DEFAULT_CAUTION
        ): ConsoleScanner<String> = ConsoleScanner(
                judge = pattern.asPredicate()::test,
                converter = { it },
                prompt = prompt,
                caution = caution
        )

        /**
         * 指定された範囲の Int 値を取得するスキャナーを生成します。
         *
         * @param startInclusive 取得したい範囲の下限値（この値を範囲に含みます）
         * @param endInclusive 取得したい範囲の上限値（この値を範囲に含みます）
         * @param prompt ユーザーに入力を促す文字列
         * @param caution ユーザーが入力を誤ったときにそれを知らせる文字列
         */
        fun forInt(
                startInclusive: Int,
                endInclusive: Int,
                prompt: String = "${startInclusive}～${endInclusive} の範囲の数値を入力してください > ",
                caution: String = DEFAULT_CAUTION
        ): ConsoleScanner<Int> = ConsoleScanner(
                judge = {
                    try {
                        it.toInt() in startInclusive..endInclusive
                    } catch (e: NumberFormatException) {
                        false
                    }
                },
                converter = String::toInt,
                prompt = prompt,
                caution = caution
        )

        /**
         * 指定された範囲の Long 値を取得するスキャナーを生成します。
         *
         * @param startInclusive 取得したい範囲の下限値（この値を範囲に含みます）
         * @param endInclusive 取得したい範囲の上限値（この値を範囲に含みます）
         * @param prompt ユーザーに入力を促す文字列
         * @param caution ユーザーが入力を誤ったときにそれを知らせる文字列
         */
        // MEMO: IntとLongの2回書かなくてよい方法、何かないのかしら・・・
        fun forLong(
                startInclusive: Long,
                endInclusive: Long,
                prompt: String = "${startInclusive}～${endInclusive} の範囲の数値を入力してください > ",
                caution: String = DEFAULT_CAUTION
        ): ConsoleScanner<Long> = ConsoleScanner(
                judge = {
                    try {
                        it.toLong() in startInclusive..endInclusive
                    } catch (e: NumberFormatException) {
                        false
                    }
                },
                converter = String::toLong,
                prompt = prompt,
                caution = caution
        )

        /**
         * 指定されたリストの中から一つの要素を選択させて返すスキャナーを生成します。
         *
         * @param list 要素選択対象のリスト
         * @param prompt ユーザーに入力を促す文字列
         * @param caution ユーザーが入力を誤ったときにそれを知らせる文字列
         */
        fun <U> forList(
                list: List<U>,
                prompt: String = {
                    val str: StringBuilder = StringBuilder()
                    str.appendln("以下の中から番号で選択してください。")
                    list.forEachIndexed { idx, item -> str.appendln("\t${idx + 1} : $item") }
                    str.append("> ")
                    str.toString()
                }(),
                caution: String = DEFAULT_CAUTION
        ): ConsoleScanner<U> = ConsoleScanner(
                judge = {
                    try {
                        it.toInt() in 1..list.lastIndex + 1
                    } catch (e: NumberFormatException) {
                        false
                    }
                },
                converter = { list[it.toInt() - 1] },
                prompt = prompt,
                caution = caution
        )

        /**
         * 指定された列挙型の要素の中から一つを選択させて返すスキャナーを生成します。
         *
         * @param enumClass 要素選択対象の列挙型
         * @param prompt ユーザーに入力を促す文字列
         * @param caution ユーザーが入力を誤ったときにそれを知らせる文字列
         */
        fun <U : Enum<out U>> forEnum(
                enumClass: KClass<U>,
                prompt: String = {
                    val enumMembers = enumClass.java.enumConstants
                    val str: StringBuilder = StringBuilder()
                    str.appendln("以下の中から番号で選択してください。")
                    enumMembers.forEachIndexed { idx, item -> str.appendln("\t${idx + 1} : $item") }
                    str.append("> ")
                    str.toString()
                }(),
                caution: String = DEFAULT_CAUTION
        ): ConsoleScanner<U> = ConsoleScanner(
                judge = {
                    try {
                        it.toInt() in 1..enumClass.java.enumConstants.lastIndex + 1
                    } catch (e: NumberFormatException) {
                        false
                    }
                },
                converter = { enumClass.java.enumConstants[it.toInt() - 1] },
                prompt = prompt,
                caution = caution
        )

        /**
         * [y/N] を選択させるスキャナーを生成します。
         *
         * @param question [y/N] で答えさせたい質問
         * @param caution ユーザーが入力を誤ったときにそれを知らせる文字列
         */
        fun yesOrNo(
                question: String,
                caution: String = DEFAULT_CAUTION
        ): ConsoleScanner<Boolean> = ConsoleScanner(
                judge = { it.toLowerCase() == "y" || it.toLowerCase() == "n" },
                converter = { it.toLowerCase() == "y" },
                prompt = "$question [y/N] > ",
                caution = caution
        )
    }

    /**
     * 標準入力から対話的にユーザー入力値を取得し、目的の型に変換して返します。
     * 要求する形式の入力値が得られるまで、ユーザーに何度も再入力を求めます。
     */
    // TODO: 割り込みに対応する。
    fun get(): T {
        while (true) {
            print(prompt)
            val input: String = readLine() ?: ""
            if (judge(input)) return converter(input)
            else print(caution)
        }
    }
}

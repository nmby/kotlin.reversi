package xyz.hotchpotch.reversi.util

import java.util.regex.Pattern
import kotlin.reflect.KClass

private const val DEFAULT_PROMPT: String = "> "
private const val DEFAULT_CAUTION: String = "入力形式が不正です。再入力してください。"

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
         * @param caution ユーザーが入力を誤ったときに再入力を促す文字列
         * @return 指定した形式の文字列を取得するスキャナー
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
         * @param caution ユーザーが入力を誤ったときに再入力を促す文字列
         * @return 指定した範囲の整数を取得するスキャナー
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
         * @param caution ユーザーが入力を誤ったときに再入力を促す文字列
         * @return 指定した範囲の整数を取得するスキャナー
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
         * @param caution ユーザーが入力を誤ったときに再入力を促す文字列
         * @return 指定されたリストの中から１つの要素を選択させるスキャナー
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
         * @param caution ユーザーが入力を誤ったときに再入力を促す文字列
         * @return 指定された列挙型の要素の中から１つを選択させるスキャナー
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
    }

    /**
     * 標準入力から対話的にユーザー入力値を取得し、目的の型に変換して返します。
     * 要求する形式の入力値が得られるまで、ユーザーに何度も再入力を求めます。
     */
    // MEMO: 割り込みに対応するとより良い。
    // @see https://github.com/nmby/toolbox/blob/master/project/src/main/java/xyz/hotchpotch/util/console/ConsoleScanner.java#L424
    fun get(): T {
        while (true) {
            print(prompt)
            val input: String = readLine() ?: ""
            if (judge.invoke(input)) return converter.invoke(input)
            else println(caution)
        }
    }
}
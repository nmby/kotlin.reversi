package xyz.hotchpotch.reversi

/**
 * 石の色を表す列挙型です。
 */
enum class Color(private val symbol: String) {

    /** 黒 */
    BLACK("●"),

    /** 白 */
    WHITE("○");

    /**
     * @return 自身と反対の色
     */
    fun reversed(): Color = if (this === BLACK) WHITE else BLACK

    // MEMO: こんな自明な型 「: String」 を書くのはきっとダサいんだろうけど、
    // 慣れるまでは全て明示的に記述することにする。
    override fun toString(): String = symbol
}

package xyz.hotchpotch.reversi.players

import xyz.hotchpotch.reversi.*
import xyz.hotchpotch.reversi.framework.Player
import xyz.hotchpotch.reversi.framework.PlayerFactory
import xyz.hotchpotch.reversi.util.ConsoleScanner

private val posRegex: Regex = "[a-%c][1-%d]"
        .format('a' + Point.WIDTH - 1, Point.HEIGHT)
        .toRegex()

/**
 * 標準入力から手動で手を入力するためのプレーヤーです。
 *
 * @param color このプレーヤーの石の色
 * @param safety ユーザーが誤った手を入力したときに再入力を求める場合は true。
 *              　ルール違反で負けになることが分かりながらこのプレーヤーの手としてそのまま採用する場合は false。
 */
class ManualPlayer(private val color: Color, private val safety: Boolean = false) : Player {
    
    companion object : PlayerFactory {
        override fun create(color: Color, millisInGame: Long, millisAtTurn: Long): Player =
                ManualPlayer(color)
    }

    /** 標準入力から手を得るためのスキャナー */
    private val scanner: ConsoleScanner<Point?> = ConsoleScanner(
            judge = { s -> s.toLowerCase().let { it == "pass" || posRegex.matches(it) } },
            converter = { s -> s.toLowerCase().let { if (it == "pass") null else Point[it] } },
            prompt = """手を選択してください。 "a1"～"%c%d" or "PASS" > """
                    .format('a' + Point.WIDTH - 1, Point.HEIGHT),
            caution = "入力形式が正しくありません。"
    )

    /** 標準入力から手を取得して返します。 */
    override fun choosePoint(board: Board, millisInGame: Long): Point? {
        while (true) {
            val chosen: Point? = scanner.get()

            if (!safety || board.canApply(Move(color, chosen))) return chosen

            print("その手はルール違反のため選べません。")
        }
    }
}
package xyz.hotchpotch.reversi.players

import xyz.hotchpotch.reversi.*
import xyz.hotchpotch.reversi.framework.Player
import xyz.hotchpotch.reversi.framework.PlayerFactory
import xyz.hotchpotch.reversi.util.ConsoleScanner

private val posRegex: Regex = "[a-%c][1-%d]".format('a' + Point.WIDTH - 1, Point.HEIGHT).toRegex()

class ManualPlayer(private val color: Color, private val safety: Boolean = false) : Player {
    companion object : PlayerFactory {
        override fun create(color: Color, millisInGame: Long, millisInTurn: Long): Player =
                ManualPlayer(color)
    }

    private val scanner: ConsoleScanner<Point?> = ConsoleScanner(
            judge = { it.toUpperCase() == "PASS" || posRegex.matches(it.toLowerCase()) },
            converter = { if (it.toUpperCase() == "PASS") null else Point[it.toLowerCase()] },
            prompt = """手を選択してください。 "a1"～"%c%d" or "PASS" > """
                    .format('a' + Point.WIDTH - 1, Point.HEIGHT),
            caution = "入力形式が正しくありません。"
    )

    override fun choosePoint(board: Board, millisInGame: Long): Point? {
        while (true) {
            val chosen: Point? = scanner.get()

            if (!safety || board.canApply(Move(color, chosen))) return chosen

            print("その手はルール違反のため選べません。")
        }
    }
}
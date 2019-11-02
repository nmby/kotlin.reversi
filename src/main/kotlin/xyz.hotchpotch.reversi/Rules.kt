package xyz.hotchpotch.reversi

// お勉強MEMO:
// このファイルの内容は Board.kt に記述してしまっても良いものだが、
// 「リバーシ盤上のどこに石を置けるか」「どの石をひっくり返せるか」というルールロジックのみ
// 切り出してみることにした。
// というか、拡張を使ってみたかっただけとう話もある。

/**
 * @return このリバーシ盤がゲーム継続中の場合（まだ石を置ける位置がある場合）に true を返します。
 */
fun Board.isGameOngoing(): Boolean = canPut(Color.BLACK) || canPut(Color.WHITE)

/**
 * @param color 石の色
 * @return このリバーシ盤に指定された色の石を置ける位置がある場合に true を返します。
 */
fun Board.canPut(color: Color): Boolean = Point.values().any { canPut(color, it) }

/**
 * @param color 石の色
 * @param point 石を置く位置
 * @return このリバーシ盤の指定された位置に指定された色の石を置ける場合に true を返します。
 */
fun Board.canPut(color: Color, point: Point): Boolean = this[point] === null
        && Direction.values().any { reversibles(color, point, it).isNotEmpty() }

/**
 * @param move 適用する手
 * @return このリバーシ盤に指定された手を適用できる場合に true を返します。
 */
fun Board.canApply(move: Move): Boolean = isGameOngoing()
        && if (move.isPass()) !canPut(move.color) else canPut(move.color, move.point!!)

/**
 * @param color 石の色
 * @param point 石を置く位置
 * @return このリバーシ盤の指定された位置に指定された色の石を置いた場合にひっくり返せる石の位置。
 *         ひっくり返せる石がない場合は空のセット
 */
fun Board.reversibles(color: Color, point: Point): Set<Point> {
    return if (this[point] !== null) emptySet()
    else Direction.values().flatMap { reversibles(color, point, it) }.toSet()
}

private fun Board.reversibles(color: Color, point: Point, direction: Direction): Set<Point> {
    val points: MutableSet<Point> = mutableSetOf()
    var p: Point? = point + direction

    while (p !== null) {
        when (this[p]) {
            color -> return points
            color.reversed() -> points.add(p)
            else -> return emptySet()
        }
        p += direction
    }
    return emptySet()
}

/**
 * @return このリバーシ盤の勝者の色を返します。引き分けの場合は null を返します。
 * @throws IllegalStateException このリバーシ盤がゲーム継続中の場合
 */
fun Board.winner(): Color? {
    check(!isGameOngoing()) { "まだゲーム継続中です。" }
    val black: Int = Point.values().count { this[it] === Color.BLACK }
    val white: Int = Point.values().count { this[it] === Color.WHITE }
    return when {
        white < black -> Color.BLACK
        black < white -> Color.WHITE
        else -> null
    }
}

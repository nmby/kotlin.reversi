package xyz.hotchpotch.reversi

// お勉強MEMO:
// このファイルの内容は Board.kt に記述してしまっても良いものだが、
// 「リバーシ盤上のどこに石を置けるか」「どの石をひっくり返せるか」という類の
// ルールに関するロジックのみ切り出してみることにした。
// というか、拡張を使ってみたかっただけという話もある。

/** このリバーシ盤がゲーム継続中の場合（まだ石を置ける位置がある場合）に true を返します。 */
fun Board.isGameOngoing(): Boolean = canPut(Color.BLACK) || canPut(Color.WHITE)

/** このリバーシ盤に指定された色の石を置ける位置がある場合に true を返します。 */
// お勉強MEMO：
// 意味が自明なパラメータの説明は省略。
// javadocに慣れたJava脳からするとソワソワするが、「形式よりも実益」を重視するのがkotlin流儀だと理解。
fun Board.canPut(color: Color): Boolean = Point.values.any { canPutAt(color, it) }

/** このリバーシ盤の指定された位置に指定された色の石を置ける場合に true を返します。 */
fun Board.canPutAt(color: Color, point: Point): Boolean = this[point] === null
        && Direction.values().any { reversibles(color, point, it).isNotEmpty() }

/** このリバーシ盤に指定された手を適用できる場合に true を返します。 */
fun Board.canApply(move: Move): Boolean = isGameOngoing()
        && if (move.isPass()) !canPut(move.color) else canPutAt(move.color, move.point!!)

/** このリバーシ盤上の指定された色の石を置ける位置が格納されたセットを返します。 */
// 「プッタブル」っていうと金融デリバティブ商品みたいだけど。
// お勉強MEMO:
// こういうAPIの戻り値型をどうするか悩む。この場合は本質的に Set であるべきだが、
// 使う側は List の方が便利かもしれない。今回は Set にしてみる。
fun Board.puttables(color: Color): Set<Point> =
        Point.values.filter { canPutAt(color, it) }.toSet()

/**
 * このリバーシ盤の指定された位置に指定された色の石を置いた場合に
 * ひっくり返せる石の位置が格納されたセット（ひっくり返せる石がない場合は空のセット）を返します。
 */
fun Board.reversibles(color: Color, point: Point): Set<Point> =
        if (this[point] !== null) emptySet()
        else Direction.values().flatMap { reversibles(color, point, it) }.toSet()

/**
 * このリバーシ盤の指定された位置に指定された色の石を置いた場合に、
 * 指定された方向にひっくり返せる石の位置が格納されたセット（ひっくり返せる石がない場合は空のセット）を返します。
 */
private fun Board.reversibles(color: Color, point: Point, direction: Direction): Set<Point> {
    assert(this[point] === null)

    val reversibles: MutableSet<Point> = mutableSetOf()
    var p: Point? = point + direction

    while (p !== null) {
        when (this[p]) {
            color -> return reversibles
            color.reversed() -> reversibles.add(p)
            else -> return emptySet()
        }
        p += direction
    }
    return emptySet()
}

/**
 * このリバーシ盤の勝者の色を返します。引き分けの場合は null を返します。
 * @throws IllegalStateException このリバーシ盤がゲーム継続中の場合
 */
fun Board.winner(): Color? {
    check(!isGameOngoing()) { "まだゲーム継続中です。" }
    val black: Int = count(Color.BLACK)
    val white: Int = count(Color.WHITE)
    return when {
        white < black -> Color.BLACK
        black < white -> Color.WHITE
        else -> null
    }
}

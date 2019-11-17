package xyz.hotchpotch.reversi.players

import xyz.hotchpotch.reversi.Board
import xyz.hotchpotch.reversi.Direction
import xyz.hotchpotch.reversi.Point

/** 指定されたリバーシ盤上の、この先決してひっくり返されない石の位置を返します。 */
fun stablePoints(board: Board): Set<Point> {

    // 安定であることが判明した石の位置を記録するセット
    val stablePoints: MutableSet<Point> = mutableSetOf()

    // まだ安定か不安定かが未判明の石の位置を記録するセット
    val unclearPoints: MutableSet<Point> =
            Point.values.filter { board[it] !== null }.toMutableSet()

    // 未判明の位置が無くなるまで繰り返す。
    while (unclearPoints.isNotEmpty()) {
        val results: Map<Point, Boolean?> =
                unclearPoints.associateWith { isStable(board, stablePoints, unclearPoints, it) }

        // 安定であることが判明したものは stablePoints に追加する。
        stablePoints.addAll(results.filter { it.value == true }.keys)

        // 安定か不安定かが判明したもの（つまり、引き続き未判明のもの以外）を unclearPoints から除く。
        unclearPoints.removeIf { results[it] !== null }
    }
    return stablePoints
}

/**
 * 指定されたリバーシ盤上の指定された位置にある石が安定である場合に true を、
 * 不安定である場合に false を、未判明である場合に null を返します。
 * 「安定である」とは、今後決してひっくり返されることがないことを意味します。
 *
 * 注意：
 * 現在の実装アルゴリズムには不完全な点があります。そのため、
 * 本来は安定である位置を不安定であると判断することがあります。
 *
 * @param board 検査時点のリバーシ盤
 * @param stablePoints これまでに判明している「安定な石」の場所
 * @param unclearPoints 安定であるか不安定であるかが未判明の場所
 * @param testee 検査対象の位置
 */
private fun isStable(
        board: Board,
        stablePoints: Set<Point>,
        unclearPoints: Set<Point>,
        testee: Point
): Boolean? {

    if (board[testee] === null) return false

    val isStableInEachDirection: List<Boolean?> = Direction.values()
            .map { isStable(board, stablePoints, unclearPoints, testee, it) }

    return when {
        // 全ての方向について安定であるならば、安定であると言える。
        isStableInEachDirection.all { it == true } -> true

        // 不安定である方向が一つでもあるならば、不安定である。
        isStableInEachDirection.any { it == false } -> false

        // 上記に当てはまらない場合は未判明である。
        else -> null
    }
}

/**
 * 指定されたリバーシ盤上の指定された位置にある石が、指定された方向について安定である場合に
 * true を、不安定である場合に false を、未判明である場合に null を返します。
 * 「指定された方向について安定である」とは、具体的には次の条件を満たす場合を指します。
 *   - 指定された位置から指定された方向に向けて、開きマスが無い場合
 *   - 開きマスがある場合であっても、反対方向に向けて相手の石が無い、かつ、入り込む余地が無い場合
 *     （つまり、リバーシ盤の端であるか、自身の色の安定な石に接している場合）
 *
 * 注意：
 * 現在の実装アルゴリズムには不完全な点があります。そのため、
 * 本来は安定である位置を不安定であると判断することがあります。
 *
 * @param board 検査時点のリバーシ盤
 * @param stablePoints これまでに判明している「安定な石」の場所
 * @param unclearPoints 安定であるか不安定であるかが未判明の場所
 * @param testee 検査対象の位置
 * @param direction 検査対象の方向
 */
// TODO: アルゴリズムの不完全な点を改善する（結構難しい）
//
// 例えば次のような石の配置のとき（パターン1）、
//
//       a b c d e f g h
//     1 ● ● ○ ● ○ ・ ・ ・
//
// e1 の ○ は不安定である。何故なら、f1 に ● が置かれることでひっくり返るからである。
// c1 の ○ も不安定である。何故なら、f1● -> g1○ -> h1● の順に石が置かれることでひっくり返るからである。
//
// 一方、次のような石の配置のとき（パターン2）、
//
//       a b c d e f g h
//     1 ● ● ● ○ ● ○ ・ ・
//
// f1 の ○ は不安定であるが、d1 の ○ は安定である。
// d1 の ○ が安定であることを正しく判定しようとすると、●○●○の縞々具合と空きマスの数を考慮する必要がある。
//
// さらに付言すると、次のような石の配置のとき（パターン3）、
//
//       a b c d e f g h
//     1 ○ ○ ○ ○ ○ ○ ○ ○
//     2 ● ● ● ○ ● ○ ・ ・
//     3 ・ ・ ・ ・ ・ ・ ・ ・
//
// 2行目の石の配置に着目すると、パターン2の1行目の石の配置と同じであるが、
// 右方向について、パターン2においてはf列の ○ は不安定、d列の ○ は安定であったのに対して、
// パターン3においてはf列の ○、d列の○ともに不安定である。
// 何故なら、e3○ -> g2● の順に石が置かれることによりひっくり返されるからである。
//
// つまり、d2 ○ の右方向についての安定性を、周囲の石の右方向だけでない全方向に関する安定性と絡めて
// 判定する必要があるということである。
private fun isStable(
        board: Board,
        stablePoints: Set<Point>,
        unclearPoints: Set<Point>,
        testee: Point,
        direction: Direction
): Boolean? {

    assert(board[testee] !== null)

    // 指定された方向に空きマスがあるか調べ、無い場合は「安定である」と判断する。
    val hasEmptyCellInTheDirection: Boolean =
            generateSequence(testee + direction, { it + direction })
                    .any { board[it] === null }
    if (!hasEmptyCellInTheDirection) return true

    // 反対方向を調べ、リバーシ盤の端であれば「安定である」と判断する。
    val oppositeNext: Point? = testee + direction.opposite
    if (oppositeNext === null) return true

    // 反対方向に隣接するマスが自身の石でない場合（つまり、空もしくは相手の石の場合）は
    // 「不安定である」と判断する。
    if (board[oppositeNext] !== board[testee]) return false

    // 反対方向に隣接するマスが自身の石の場合
    return when {
        // 隣接する石が安定であるなら、この石も安定である。
        stablePoints.contains(oppositeNext) -> true

        // 隣接する石が安定であるか不安定であるか未判明であるなら、この石についても未判明である。
        unclearPoints.contains(oppositeNext) -> null

        // 隣接する石が不安定であるなら、この石も不安定である。
        else -> false
    }
}

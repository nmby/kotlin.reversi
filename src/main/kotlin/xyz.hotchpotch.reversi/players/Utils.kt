package xyz.hotchpotch.reversi.players

import xyz.hotchpotch.reversi.Board
import xyz.hotchpotch.reversi.Direction
import xyz.hotchpotch.reversi.Point

/** 指定されたリバーシ盤について、この先決してひっくり返されない石の位置を返します。 */
fun stablePoints(board: Board): Set<Point> {

    // 安定であることが判明した石の位置を記録するセット
    val stablePoints: MutableSet<Point> = mutableSetOf()

    // まだ安定か安定でないかが未判明の石の位置を記録するセット
    val unclearPoints: MutableSet<Point> =
            Point.values.filter { board[it] !== null }.toMutableSet()

    // 未判明の位置が無くなるまで繰り返す。
    while (unclearPoints.isNotEmpty()) {
        val results: Map<Point, Boolean?> =
                unclearPoints.associateWith { isStable(board, stablePoints, unclearPoints, it) }

        // 安定であることが判明したものは stablePoints に追加する。
        stablePoints.addAll(results.filter { it.value == true }.keys)

        // 安定であるか安定でないかが判明したもの（つまり、引き続き未判明のもの以外）を unclearPoints から除く。
        unclearPoints.removeIf { results[it] !== null }
    }
    return stablePoints
}

/**
 * 指定されたリバーシ盤上の指定された位置にある石が安定である場合に true を返します。
 * 「安定である」とは、今後決してひっくり返されることがないことを意味します。
 *
 * 注意：
 * 現在の実装アルゴリズムには不完全な点があります。そのため、
 * 本来は安定である位置を安定でないと判断することがあります。
 *
 * @param board 検査時点のリバーシ盤
 * @param stablePoints これまでに判明している「安定な石」の場所
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
        isStableInEachDirection.all { it == true } -> true
        isStableInEachDirection.any { it == false } -> false
        else -> null
    }
}

/**
 * 指定されたリバーシ盤上の指定された位置にある石が、指定された方向について安定である場合に
 * true を返します。
 * 「指定された方向について安定である」とは、具体的には次の条件を満たす場合を指します。
 *   - 指定された位置から指定された方向に向けて、開きマスが無い場合
 *   - 開きマスがある場合であっても、反対方向に向けて相手の石が無い、かつ、入り込む余地が無い場合
 *     （つまり、リバーシ盤の端であるか、自身の色の安定な石に接している場合）
 *
 * 注意：
 * 現在の実装アルゴリズムには不完全な点があります。そのため、
 * 本来は安定である位置を安定でないと判断することがあります。
 * TODO: アルゴリズムを改善する（結構難しい）
 *
 * @param board 検査時点のリバーシ盤
 * @param stablePoints これまでに判明している「安定な石」の場所
 * @param unclearPoints 安定であるか否かが判定していない場所
 * @param testee 検査対象の位置
 * @param direction 検査対象の方向
 */
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
    // 「安定ではない」と判断する。
    if (board[oppositeNext] !== board[testee]) return false

    // 反対方向に隣接するマスが自身の石の場合
    return when {
        // 隣接する石が安定であるなら、この石も安定である。
        stablePoints.contains(oppositeNext) -> true

        // 隣接する石が安定であるか否か不明であるなら、この石についてもまだ判断は下せない。
        unclearPoints.contains(oppositeNext) -> null

        // 隣接する石が安定でないなら、この石も安定でない。
        else -> false
    }
}

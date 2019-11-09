package xyz.hotchpotch.reversi

/**
 * リバーシ盤上の方向を表す列挙型です。
 *
 * @property di 縦方向のベクトル
 * @property dj 横方向のベクトル
 */
enum class Direction(internal val di: Int, internal val dj: Int) {

    /** 上 */
    UPPER(-1, 0),

    /** 右上 */
    UPPER_RIGHT(-1, 1),

    /** 右 */
    RIGHT(0, 1),

    /** 右下 */
    LOWER_RIGHT(1, 1),

    /** 下 */
    LOWER(1, 0),

    /** 左下 */
    LOWER_LEFT(1, -1),

    /** 左 */
    LEFT(0, -1),

    /** 左上 */
    UPPER_LEFT(-1, -1);
}

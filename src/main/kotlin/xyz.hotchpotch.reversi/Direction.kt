package xyz.hotchpotch.reversi

/**
 * リバーシ盤上の方向を表す列挙型です。
 *
 * @property di 縦方向のベクトル
 * @property dj 横方向のベクトル
 */
enum class Direction(internal val di: Int, internal val dj: Int, opposite: Lazy<Direction>) {

    /** 上 */
    UPPER(-1, 0, lazy { LOWER }),

    /** 右上 */
    UPPER_RIGHT(-1, 1, lazy { LOWER_LEFT }),

    /** 右 */
    RIGHT(0, 1, lazy { LEFT }),

    /** 右下 */
    LOWER_RIGHT(1, 1, lazy { UPPER_LEFT }),

    /** 下 */
    LOWER(1, 0, lazy { UPPER }),

    /** 左下 */
    LOWER_LEFT(1, -1, lazy { UPPER_RIGHT }),

    /** 左 */
    LEFT(0, -1, lazy { RIGHT }),

    /** 左上 */
    UPPER_LEFT(-1, -1, lazy { LOWER_RIGHT });

    /** 自身と反対の方向 */
    val opposite: Direction by opposite
}

package xyz.hotchpotch.reversi.framework

import xyz.hotchpotch.reversi.Board
import xyz.hotchpotch.reversi.Color
import xyz.hotchpotch.reversi.Point
import xyz.hotchpotch.reversi.players.ManualPlayer
import xyz.hotchpotch.reversi.players.MonteCarloPlayer
import xyz.hotchpotch.reversi.players.RandomPlayer
import xyz.hotchpotch.reversi.players.SimplestPlayer
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance

/**
 * リバーシのプレーヤーを表します。
 *
 * [Player] 実装クラスは、[PlayerFactory] を実装したコンパニオンオブジェクトを持つ必要があります。
 * 詳細は [PlayerFactory.create] の説明を参照してください。
 */
interface Player {

    companion object {

        /** 既知の [Player] 実装クラスの一覧 */
        // 本当はリフレクションか何かで実現したいところ
        val implementations: List<KClass<out Player>>
            // お勉強MEMO:
            // こういうの、思わずカスタムゲッターではなくそのまま初期値として代入してしまいそうになる。
            // 気を付ける必要あり。
            // というか、Kotlin の流儀では可変オブジェクトをそのまま返しちゃってもよいのか？
            // いや、オブジェクト指向である以上、それはないはず
            get() = listOf(
                    ManualPlayer::class,
                    SimplestPlayer::class,
                    RandomPlayer::class,
                    MonteCarloPlayer::class
            )
    }

    /**
     * @param board 現在のリバーシ盤
     * @param millisInGame ゲーム内の残り持ち時間（ミリ秒）。手番ごとに消費した時間が減算されます。
     * @return 今回の手として石を打つ場所。パスの場合は null
     */
    fun choosePoint(board: Board, millisInGame: Long): Point?
}

/**
 * [Player] 実装クラスのファクトリを表します。
 */
interface PlayerFactory {

    /**
     * プレーヤーインスタンスを生成します。
     *
     * [Player] 実装クラスは、このインタフェースを実装したコンパニオンオブジェクトを持つ必要があります。
     * リバーシフレームワークは、[Player] 実装クラスのコンパニオンオブジェクトが実装するこのメソッドを利用して
     * [Player] 実装クラスをインスタンス化します。
     * [Player] 実装クラスは、パラメータとして渡された値を（少なくとも [color] は）記憶しておく必要があります。
     *
     * リバーシフレームワークは、ゲーム開始時点でこのメソッドを利用してインスタンスを取得し、ゲームの終了とともに破棄します。
     * 同じインスタンスを複数ゲームに跨って利用することはありません。
     *
     * @param color このプレーヤーの石の色
     * @param millisInGame ゲーム内の持ち時間（ミリ秒）
     * @param millisInTurn 一手当たりの持ち時間（ミリ秒）
     * @return プレーヤーインスタンス
     */
    fun create(color: Color, millisInGame: Long, millisInTurn: Long): Player
}

/**
 * 指定された [Player] 実装クラスのインスタンスを生成して返します。
 *
 * @param playerClass インスタンス化する [Player] 実装クラス
 * @param color このプレーヤーの石の色
 * @param millisInGame ゲーム内の持ち時間（ミリ秒）
 * @param millisInTurn 一手当たりの持ち時間（ミリ秒）
 * @return 生成されたプレーヤーインスタンス
 * @throws IllegalArgumentException 指定された [Player] 実装クラスのコンパニオンオブジェクトが [PlayerFactory] を実装しない場合
 */
fun createPlayer(
        playerClass: KClass<out Player>,
        color: Color,
        millisInGame: Long,
        millisInTurn: Long
): Player {

    val factory: Any? = playerClass.companionObjectInstance
    return if (factory is PlayerFactory) factory.create(color, millisInGame, millisInTurn)
    else throw IllegalArgumentException(
            "Player実装クラスはPlayerFactoryインタフェースを実装したコンパニオンオブジェクトを持つ必要があります。")
}

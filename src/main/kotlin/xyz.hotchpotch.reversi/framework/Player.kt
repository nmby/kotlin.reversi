package xyz.hotchpotch.reversi.framework

import xyz.hotchpotch.reversi.Board
import xyz.hotchpotch.reversi.Color
import xyz.hotchpotch.reversi.Point
import xyz.hotchpotch.reversi.players.*
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

        /** 既知の自動プレーヤーの一覧 */
        // 本当はリフレクションか何かで実現したいところ
        val aiPlayers: List<KClass<out Player>> = listOf(
                SimplestPlayer::class,
                RandomPlayer::class,
                RuleBasedPlayer::class,
                DepthFirstPlayer::class,
                WidthFirstPlayer::class,
                MonteCarloPlayer::class,
                MixedPlayer::class
        )
            // お勉強MEMO：
            // 可変オブジェクトをそのまま返す訳にはいかないので、
            // コピーを返すためにカスタムゲッターを定義。
            get() = field.toList()

        /** 手動プレーヤーも含む既知の [Player] 実装クラスの一覧 */
        val allPlayers: List<KClass<out Player>> = aiPlayers + ManualPlayer::class
            get() = field.toList()
    }

    /**
     * 今回の手として石を打つ位置（パスの場合は null）を選択して返します。
     *
     * リバーシフレームワークは、このプレーヤーの手番になるたびにこの関数を実行し、
     * このプレーヤーに手の選択を求めます。
     * パスの場合もこの関数が実行されるため、[Player] 実装クラスは正しく null を返す必要があります。
     *
     * 次の場合はこのプレーヤーの負けとなります。
     *   - ルール違反の手を返した場合
     *   - 制限時間を超過した場合
     *   - 何らかの例外を発生させた場合
     *
     * @param board 現在のリバーシ盤
     * @param millisInGame ゲーム内の残り持ち時間（ミリ秒）。手番ごとに消費した時間が減算されます。
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
     * リバーシフレームワークは、ゲーム開始時点でこのメソッドを利用してインスタンスを取得し、
     * ゲームの終了とともに破棄します。
     * 同じインスタンスを複数ゲームに跨って利用することはありません。
     *
     * @param color このプレーヤーの石の色
     * @param millisInGame ゲーム内の持ち時間（ミリ秒）
     * @param millisAtTurn 一手当たりの制限時間（ミリ秒）
     * @return プレーヤーインスタンス
     */
    fun create(color: Color, millisInGame: Long, millisAtTurn: Long): Player
}

/**
 * 指定された [Player] 実装クラスのインスタンスを生成して返します。
 *
 * @param playerClass インスタンス化する [Player] 実装クラス
 * @param color このプレーヤーの石の色
 * @param millisInGame ゲーム内の持ち時間（ミリ秒）
 * @param millisAtTurn 一手当たりの制限時間（ミリ秒）
 * @return 生成されたプレーヤーインスタンス
 * @throws IllegalArgumentException 指定された [Player] 実装クラスのコンパニオンオブジェクトが
 *                                  [PlayerFactory] を実装しない場合
 */
fun createPlayer(
        playerClass: KClass<out Player>,
        color: Color,
        millisInGame: Long,
        millisAtTurn: Long
): Player {

    val factory: Any? = playerClass.companionObjectInstance
    return if (factory is PlayerFactory) factory.create(color, millisInGame, millisAtTurn)
    else throw IllegalArgumentException(
            "Player実装クラスはPlayerFactoryインタフェースを実装したコンパニオンオブジェクトを持つ必要があります。")
}

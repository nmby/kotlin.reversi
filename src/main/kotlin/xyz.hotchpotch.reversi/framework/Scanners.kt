package xyz.hotchpotch.reversi.framework

import xyz.hotchpotch.reversi.util.ConsoleScanner
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

// お勉強MEMO:
// こういうユーティリティ系機能はどこに置くのがよいのだろう。

/** [Playable] 実装クラスの実行条件を標準入力から取得するためのスキャナーを集めたものです。 */
object Scanners {

    private fun playersList(includeManualPlayer: Boolean): String =
            (if (includeManualPlayer) Player.allPlayers else Player.aiPlayers)
                    .mapIndexed { idx, playerClass -> "\t${idx + 1} : ${playerClass.qualifiedName}" }
                    .joinToString("\n")

    /**
     * プレーヤークラスを取得するためのスキャナー
     *
     * @param playerDesc 標準出力に表示するプレーヤーの呼称（"●プレーヤー" や "プレーヤーA" など）
     * @param includeManualPlayer 手動プレーヤーも対象に含める場合は true
     */
    fun getPlayer(playerDesc: String, includeManualPlayer: Boolean): KClass<out Player> {
        val players: List<KClass<out Player>> =
                if (includeManualPlayer) Player.allPlayers else Player.aiPlayers

        val scanner: ConsoleScanner<Int> = ConsoleScanner.forInt(
                startInclusive = 1,
                endInclusive = players.size + 1,
                prompt = "${playersList(includeManualPlayer)}\n" +
                        "\t${players.size + 1} : その他（自作プレーヤー）\n" +
                        "${playerDesc}を番号で選択してください > "
        )

        while (true) {
            val selected: Int = scanner.get()
            if (selected <= players.size) return players[selected - 1]
            getCustomPlayer()?.let { return it }
        }
    }

    fun getCustomPlayer(): KClass<out Player>? {
        print("プレーヤークラスの名前（例: xyz.hotchpotch.reversi.players.RandomPlayer）を入力してください > ")
        val className: String = readLine() ?: ""
        val clazz: KClass<out Any>
        try {
            clazz = Class.forName(className).kotlin
        } catch (e: ClassNotFoundException) {
            println("指定されたクラスが見つかりません。")
            return null
        }
        if (!clazz.isSubclassOf(Player::class)) {
            println("指定されたクラスは ${Player::class.simpleName} インタフェースを実装していません。")
            return null
        }
        return clazz as KClass<out Player>
    }

    private const val minInGame: Long = 100
    private const val maxInGame: Long = 1000 * 60 * 60

    /** ゲーム内の持ち時間（ミリ秒）を取得するためのスキャナー */
    val millisInGame: ConsoleScanner<Long> = ConsoleScanner.forLong(
            startInclusive = minInGame,
            endInclusive = maxInGame,
            prompt = "ゲーム内の持ち時間（ミリ秒）を ${minInGame}～${maxInGame} の範囲で指定してください > "
    )

    private const val minAtTurn: Long = 50
    private const val maxAtTurn: Long = 1000 * 60 * 10

    /** 一手当たりの制限時間（ミリ秒）を取得するためのスキャナー */
    val millisAtTurn: ConsoleScanner<Long> = ConsoleScanner.forLong(
            startInclusive = minAtTurn,
            endInclusive = maxAtTurn,
            prompt = "一手ごとの制限時間（ミリ秒）を ${minAtTurn}～${maxAtTurn} の範囲で指定してください > "
    )

    private const val minTimes: Int = 2
    private const val maxTimes: Int = 1000

    /** 対戦回数を取得するためのスキャナー */
    val times: ConsoleScanner<Int> = ConsoleScanner.forInt(
            startInclusive = minTimes,
            endInclusive = maxTimes,
            prompt = "対戦回数を ${minTimes}～${maxTimes} の範囲で指定してください > "
    )

    /** 自動実行モードか否かを取得するためのスキャナー */
    val automatic: ConsoleScanner<Boolean> = ConsoleScanner.yesOrNo(
            question = "自動実行モードにしますか？")
}

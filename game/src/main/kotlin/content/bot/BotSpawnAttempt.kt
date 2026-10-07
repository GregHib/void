package content.bot

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import world.gregs.voidps.engine.entity.character.player.Player

/** Keeps incomplete spawns out of saves and releases reservations on every failure path. */
internal class BotSpawnAttempt(val name: String) {
    var player: Player? = null
        private set
    private var completed = false

    fun attach(player: Player) {
        this.player = player
        player["bot_spawn_pending"] = true
    }

    fun complete() {
        player?.clear("bot_spawn_pending")
        completed = true
    }

    suspend fun run(action: suspend BotSpawnAttempt.() -> Unit, cleanup: suspend () -> Unit, release: () -> Unit) {
        try {
            action()
        } finally {
            if (!completed) {
                withContext(NonCancellable) {
                    try {
                        cleanup()
                    } finally {
                        release()
                    }
                }
            }
        }
    }
}

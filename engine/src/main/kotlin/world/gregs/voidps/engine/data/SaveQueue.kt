package world.gregs.voidps.engine.data

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.entity.character.player.Player
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext
import kotlin.system.measureTimeMillis

class SaveQueue(
    private val storage: AccountStorage,
    override val coroutineContext: CoroutineContext = Dispatchers.IO
) : Runnable, CoroutineScope {
    private val pending = ConcurrentHashMap<String, PlayerSave>()
    private val logger = InlineLogger()

    override fun run() {
        if (pending.isEmpty()) {
            return
        }
        val accounts = pending.values.toList()
        launch {
            try {
                val took = measureTimeMillis {
                    storage.save(accounts)
                    for (account in accounts) {
                        pending.remove(account.name)
                    }
                }
                logger.info { "Saved ${accounts.size} ${"account".plural(accounts.size)} in ${took}ms" }
            } catch (e: Exception) {
                logger.error(e) { "Error saving players!" }
            }
        }
    }

    fun save(player: Player) {
        if (player.contains("bot")) {
            return
        }
        pending[player.accountName] = player.copy()
    }

    fun saving(name: String) = pending.containsKey(name)

}
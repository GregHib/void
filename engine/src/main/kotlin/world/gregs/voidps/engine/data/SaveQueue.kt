package world.gregs.voidps.engine.data

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.*
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.entity.character.player.Player
import java.lang.Runnable
import java.util.concurrent.ConcurrentHashMap
import kotlin.system.measureTimeMillis

class SaveQueue(
    private val storage: AccountStorage,
    private val fallback: AccountStorage = storage,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) : Runnable {
    private val pending = ConcurrentHashMap<String, PlayerSave>()
    private val logger = InlineLogger()

    private val handler = CoroutineExceptionHandler { _, exception ->
        logger.error(exception) { "Error saving players!" }
        scope.fallback(pending.values.toList())
    }
    private val fallbackHandler = CoroutineExceptionHandler { _, exception ->
        logger.error(exception) { "Fallback save failed!" }
    }

    override fun run() {
        if (pending.isEmpty()) {
            return
        }
        scope.save(pending.values.toList())
    }

    private fun CoroutineScope.save(accounts: List<PlayerSave>) = launch(handler) {
        val took = measureTimeMillis {
            withContext(NonCancellable) {
                storage.save(accounts)
                for (account in accounts) {
                    pending.remove(account.name)
                }
            }
        }
        logger.info { "Saved ${accounts.size} ${"account".plural(accounts.size)} in ${took}ms" }
    }

    private fun CoroutineScope.fallback(accounts: List<PlayerSave>) = launch(fallbackHandler) {
        withContext(NonCancellable) {
            fallback.save(accounts)
            for (account in accounts) {
                pending.remove(account.name)
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

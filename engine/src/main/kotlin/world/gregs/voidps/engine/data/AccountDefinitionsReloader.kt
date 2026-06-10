package world.gregs.voidps.engine.data

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import world.gregs.voidps.engine.Contexts
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.entity.character.player.Players
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Reloads offline account definitions from [storage] so external changes
 * (website password resets, imported accounts) apply without a server restart.
 */
class AccountDefinitionsReloader(
    private val storage: Storage,
    private val definitions: AccountDefinitions,
    private val saveQueue: SaveQueue,
    io: CoroutineDispatcher = Dispatchers.IO,
    private val game: CoroutineDispatcher = Contexts.Game,
) {
    private val logger = InlineLogger()
    private val reloading = AtomicBoolean(false)
    // SupervisorJob so a failed reload doesn't cancel the scope and kill future reloads
    private val scope = CoroutineScope(SupervisorJob() + io)
    private val handler = CoroutineExceptionHandler { _, exception ->
        logger.error(exception) { "Error reloading account definitions!" }
        reloading.set(false)
    }

    /**
     * Loads account data on the io dispatcher and merges it on the game thread.
     * Online players and accounts with pending saves are skipped as their
     * in-memory state may be newer than storage.
     * @return false if a reload is already in progress
     */
    fun reload(onComplete: (Int) -> Unit = {}): Boolean {
        if (!reloading.compareAndSet(false, true)) {
            return false
        }
        val online = Players.mapTo(HashSet()) { it.accountName.lowercase() }
        scope.launch(handler) {
            val names = storage.names()
            val clans = storage.clans()
            withContext(game) {
                val count = definitions.merge(names, clans) { account ->
                    online.contains(account.lowercase()) || saveQueue.saving(account)
                }
                reloading.set(false)
                logger.info { "Reloaded $count account definitions." }
                onComplete(count)
            }
        }
        return true
    }
}

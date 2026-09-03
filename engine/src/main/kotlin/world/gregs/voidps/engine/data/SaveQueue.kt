package world.gregs.voidps.engine.data

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.*
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import java.lang.Runnable
import java.util.concurrent.ConcurrentHashMap
import kotlin.system.measureTimeMillis

class SaveQueue(
    private val storage: Storage,
    private val fallback: Storage = storage,
    // SupervisorJob so a failed save doesn't cancel the scope and kill future saves
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
) : Runnable {
    private val pending = ConcurrentHashMap<String, PlayerSave>()
    private val logger = InlineLogger()
    private var job: Job? = null

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
        val job = job
        if (job != null && job.isActive) {
            return
        }
        this.job = scope.save(pending.values.toList())
    }

    fun direct(): Job {
        val online = Players.filter { !it.contains("bot") }.map { it.copy() }
        val names = online.mapTo(HashSet()) { it.name }
        val queued = pending.values.filter { it.name !in names }
        return scope.save(online + queued)
    }

    suspend fun awaitInFlight() {
        job?.join()
    }

    private fun CoroutineScope.save(accounts: List<PlayerSave>) = launch(handler) {
        val took = measureTimeMillis {
            withContext(NonCancellable) {
                storage.save(accounts)
                clearPending(accounts)
            }
        }
        logger.info { "Saved ${accounts.size} ${"account".plural(accounts.size)} in ${took}ms" }
    }

    private fun CoroutineScope.fallback(accounts: List<PlayerSave>) = launch(fallbackHandler) {
        withContext(NonCancellable) {
            fallback.save(accounts)
            clearPending(accounts)
        }
    }

    private fun clearPending(accounts: List<PlayerSave>) {
        for (account in accounts) {
            pending.computeIfPresent(account.name) { _, current ->
                if (current === account) null else current
            }
        }
    }

    fun save(player: Player) {
        if (player.contains("bot") || Settings["storage.disabled", false]) {
            return
        }
        pending[player.accountName] = player.copy()
    }

    fun saving(name: String) = pending.containsKey(name)

    fun empty(): Boolean = pending.isEmpty()
}

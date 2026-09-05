package world.gregs.voidps.engine.data

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.*
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import java.lang.Runnable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class SaveQueue(
    private val storage: Storage,
    private val fallback: Storage = storage,
    // SupervisorJob so a failed save doesn't cancel the scope and kill future saves
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
    private val retryMillis: Long = TimeUnit.MINUTES.toMillis(Settings["storage.save.retryMinutes", 5].toLong()),
) : Runnable {
    private val pending = ConcurrentHashMap<String, PlayerSave>()
    private val failing = ConcurrentHashMap<String, Long>()
    private val logger = InlineLogger()
    private var job: Job? = null

    private val handler = CoroutineExceptionHandler { _, exception ->
        logger.error(exception) { "Unexpected error in the save queue!" }
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
        return scope.save(online + queued, retry = false)
    }

    suspend fun awaitInFlight() {
        job?.join()
    }

    private fun CoroutineScope.save(accounts: List<PlayerSave>, retry: Boolean = true) = launch(handler) {
        withContext(NonCancellable) {
            val start = System.currentTimeMillis()
            try {
                storage.save(accounts)
            } catch (e: Exception) {
                failed(accounts, e, retry)
                return@withContext
            }
            clearPending(accounts)
            clearFailing(accounts)
            logger.info { "Saved ${accounts.size} ${"account".plural(accounts.size)} in ${System.currentTimeMillis() - start}ms" }
        }
    }

    private fun failed(accounts: List<PlayerSave>, exception: Exception, retry: Boolean) {
        val exhausted = if (retry) exhausted(accounts) else accounts
        if (exhausted.isEmpty()) {
            logger.error(exception) { "Error saving ${accounts.size} ${"account".plural(accounts.size)}, retrying next tick." }
            return
        }
        logger.error(exception) { "Giving up on ${exhausted.size} ${"account".plural(exhausted.size)}, writing to fallback storage: ${exhausted.joinToString { it.name }}" }
        try {
            fallback.save(exhausted)
        } catch (e: Exception) {
            logger.error(e) { "Fallback save failed!" }
        }
        clearPending(exhausted)
        clearFailing(exhausted)
    }

    private fun exhausted(accounts: List<PlayerSave>): List<PlayerSave> {
        val now = System.currentTimeMillis()
        return accounts.filter { now - (failing.putIfAbsent(it.name, now) ?: now) >= retryMillis }
    }

    private fun clearFailing(accounts: List<PlayerSave>) {
        for (account in accounts) {
            failing.remove(account.name)
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

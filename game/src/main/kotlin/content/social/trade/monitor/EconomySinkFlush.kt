package content.social.trade.monitor

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.Storage
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

/**
 * Periodically drains [EconomySink] to database storage and prunes rows
 * older than the retention window. Only active when the storage backend is
 * a database; file installs rely on the AuditLog TSVs alone.
 */
class EconomySinkFlush(private val storage: Storage) : Script {

    private val logger = InlineLogger()
    private val scope = CoroutineScope(Dispatchers.IO)
    private val handler = CoroutineExceptionHandler { _, exception ->
        logger.error(exception) { "Error saving economy monitor rows!" }
    }
    private var lastPrune = 0L

    init {
        worldSpawn {
            reload()
            if (EconomySink.enabled) {
                World.timers.start("economy_sink_flush")
            }
        }

        worldTimerStart("economy_sink_flush") { TimeUnit.MINUTES.toTicks(Settings["economy.monitor.flushMinutes", 1]) }
        worldTimerTick("economy_sink_flush") {
            if (!EconomySink.enabled) {
                return@worldTimerTick Timer.CANCEL
            }
            flush()
            TimeUnit.MINUTES.toTicks(Settings["economy.monitor.flushMinutes", 1])
        }
        worldTimerStop("economy_sink_flush") { shutdown ->
            if (shutdown) {
                flush()
            }
        }

        settingsReload {
            reload()
            if (EconomySink.enabled) {
                World.timers.start("economy_sink_flush", restart = true)
            } else {
                World.timers.stop("economy_sink_flush")
            }
        }
    }

    private fun reload() {
        EconomySink.enabled = Settings["storage.type", "files"] == "database" &&
            Settings["economy.monitor.enabled", false] &&
            !Settings["storage.disabled", false]
    }

    private fun flush() {
        val flags = EconomySink.drainFlags()
        val census = EconomySink.drainCensus()
        val now = System.currentTimeMillis()
        val prune = now - lastPrune >= TimeUnit.HOURS.toMillis(1)
        if (prune) {
            lastPrune = now
        }
        if (flags.isEmpty() && census.isEmpty() && !prune) {
            return
        }
        val before = now - TimeUnit.DAYS.toMillis(Settings["economy.monitor.retentionDays", 90].toLong())
        scope.launch(handler) {
            withContext(NonCancellable) {
                if (flags.isNotEmpty()) {
                    storage.saveEconomyFlags(flags)
                }
                if (census.isNotEmpty()) {
                    storage.saveCensusSnapshots(census)
                }
                if (prune) {
                    storage.pruneEconomy(before)
                }
            }
        }
    }
}

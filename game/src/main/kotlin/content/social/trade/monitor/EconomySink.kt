package content.social.trade.monitor

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.data.CensusSnapshot
import world.gregs.voidps.engine.data.EconomyFlag

/**
 * Buffers economy monitor flags and census snapshots for database persistence.
 * Rows buffered between flushes are lost on a hard crash - the AuditLog TSV
 * remains the durable record; the database is the queryable copy.
 * Note: not thread safe; only use within game thread. [EconomySinkFlush]
 * drains via list swap, transferring ownership to the IO coroutine.
 */
object EconomySink {

    private const val MAX_BUFFER = 10_000
    private val logger = InlineLogger()

    var enabled = false

    private var flags = mutableListOf<EconomyFlag>()
    private var census = mutableListOf<CensusSnapshot>()
    private var warned = false

    fun flag(type: String, source: String? = null, target: String? = null, item: String? = null, value: Long = 0, details: String? = null) {
        if (!enabled) {
            return
        }
        if (flags.size >= MAX_BUFFER) {
            warn()
            return
        }
        flags.add(EconomyFlag(type, System.currentTimeMillis(), GameLoop.tick, source, target, item, value, details))
    }

    fun census(counts: List<ItemCensus.Count>) {
        if (!enabled) {
            return
        }
        val timestamp = System.currentTimeMillis()
        for (count in counts) {
            if (count.total == 0L) {
                continue
            }
            if (census.size >= MAX_BUFFER) {
                warn()
                return
            }
            census.add(CensusSnapshot(timestamp, count.item, count.players, count.floor, count.exchange))
        }
    }

    private fun warn() {
        if (!warned) {
            warned = true
            logger.warn { "Economy sink buffer full; dropping rows until next flush." }
        }
    }

    fun drainFlags(): List<EconomyFlag> {
        val drained = flags
        flags = mutableListOf()
        warned = false
        return drained
    }

    fun drainCensus(): List<CensusSnapshot> {
        val drained = census
        census = mutableListOf()
        return drained
    }

    fun clear() {
        flags = mutableListOf()
        census = mutableListOf()
        enabled = false
        warned = false
    }
}

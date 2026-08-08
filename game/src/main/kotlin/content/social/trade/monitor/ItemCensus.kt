package content.social.trade.monitor

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.exchange.OpenOffers
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.inv.InventorySlotChanged

/**
 * Tracks how many of each watched item exist in the economy, split into
 * three buckets: player inventories (all accounts, online and offline),
 * floor items and open grand exchange sell offers.
 * Container moves are net-zero; a dupe shows as unexplained total growth
 * between snapshots, exposed by periodic `CENSUS` audit lines and verified
 * by reconciliation scans against player saves (`CENSUS_DRIFT`).
 */
object ItemCensus {

    private val watched = mutableSetOf<String>()
    private val players = Object2LongOpenHashMap<String>()
    private val floor = Object2LongOpenHashMap<String>()

    var enabled = false
        private set

    fun reload() {
        enabled = Settings["economy.monitor.enabled", false] && Settings["economy.monitor.census.enabled", false]
        watched.clear()
        if (!enabled) {
            return
        }
        val threshold = Settings["economy.monitor.census.valueThreshold", 1_000_000]
        for ((id, index) in ItemDefinitions.ids) {
            if (id.endsWith("_noted")) {
                continue
            }
            if (ItemDefinitions.get(index).cost >= threshold) {
                watched.add(id)
            }
        }
        for (id in Settings["economy.monitor.census.items", ""].split(",")) {
            if (id.isNotBlank()) {
                watched.add(id.trim())
            }
        }
    }

    fun watched(): Set<String> = watched

    fun canonical(id: String): String = id.removeSuffix("_noted")

    fun change(change: InventorySlotChanged) {
        count(players, change.fromItem, -1)
        count(players, change.item, +1)
    }

    fun spawn(item: FloorItem) {
        count(floor, item.id, item.amount.toLong())
    }

    fun despawn(item: FloorItem) {
        count(floor, item.id, -item.amount.toLong())
    }

    private fun count(bucket: Object2LongOpenHashMap<String>, item: Item, sign: Int) {
        if (item.isEmpty() || item.amount <= 0) {
            return
        }
        count(bucket, item.id, sign * item.amount.toLong())
    }

    private fun count(bucket: Object2LongOpenHashMap<String>, id: String, delta: Long) {
        val canonical = canonical(id)
        if (canonical !in watched) {
            return
        }
        bucket.addTo(canonical, delta)
    }

    /**
     * Counts of watched items escrowed in open grand exchange offers:
     * remaining items in sell offers, plus coins escrowed in buy offers.
     */
    fun exchange(offers: OpenOffers): Map<String, Long> {
        val counts = Object2LongOpenHashMap<String>()
        for (item in watched) {
            var total = 0L
            for (list in offers.selling(item).values) {
                for (offer in list) {
                    total += offer.remaining
                }
            }
            if (total > 0) {
                counts.addTo(item, total)
            }
        }
        if ("coins" in watched) {
            for (offersByPrice in offers.buyByItem.values) {
                for ((price, list) in offersByPrice) {
                    for (offer in list) {
                        counts.addTo("coins", price.toLong() * offer.remaining)
                    }
                }
            }
        }
        return counts
    }

    fun snapshot(offers: OpenOffers): List<Count> {
        val exchange = exchange(offers)
        return watched.map { item ->
            Count(item, players.getLong(item), floor.getLong(item), exchange[item] ?: 0L)
        }
    }

    /**
     * Overwrite the live player-bucket counters with [actual] counts from a
     * reconciliation scan, logging any items that drifted beyond tolerance.
     */
    fun apply(actual: Map<String, Long>) {
        val tolerance = Settings["economy.monitor.census.driftPercent", 0.01]
        for (item in watched) {
            val expected = players.getLong(item)
            val count = actual[item] ?: 0L
            if (expected != count) {
                val drift = Math.abs(expected - count)
                if (drift > tolerance * maxOf(count, 1L)) {
                    AuditLog.info("CENSUS_DRIFT\t$item\texpected=$expected\tactual=$count")
                    EconomySink.flag("census_drift", item = item, value = count, details = "expected=$expected")
                }
                players.put(item, count)
            }
        }
    }

    fun restore(counts: Map<String, Long>) {
        for ((item, count) in counts) {
            if (item in watched) {
                players.put(item, count)
            }
        }
    }

    fun clear() {
        watched.clear()
        players.clear()
        floor.clear()
        enabled = false
    }

    data class Count(val item: String, val players: Long, val floor: Long, val exchange: Long) {
        val total: Long
            get() = players + floor + exchange
    }
}

package content.social.trade.monitor

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.Storage
import world.gregs.voidps.engine.data.definition.InventoryDefinitions
import world.gregs.voidps.engine.data.exchange.OpenOffers
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * Maintains the live [ItemCensus] counters, logs and persists periodic
 * snapshots, and drift-corrects the player bucket with reconciliation scans
 * of every account save. All flags go to [AuditLog] only.
 */
class ItemCensusMonitor(
    private val offers: OpenOffers,
    private val storage: Storage,
) : Script {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val scanned = AtomicReference<Map<String, Long>?>(null)

    init {
        worldSpawn {
            ItemCensus.reload()
            if (!ItemCensus.enabled) {
                return@worldSpawn
            }
            ItemCensus.restore(CensusStore.load())
            World.timers.start("economy_census_snapshot")
            if (Settings["economy.monitor.census.reconcileHours", 6] > 0) {
                World.timers.start("economy_census_reconcile")
            }
            reconcile()
        }

        slotChanged("*") { change ->
            if (!ItemCensus.enabled) {
                return@slotChanged
            }
            if (change.inventory.startsWith("_") || InventoryDefinitions.get(change.inventory)["shop", false]) {
                return@slotChanged
            }
            ItemCensus.change(change)
        }

        floorItemSpawn {
            if (ItemCensus.enabled) {
                ItemCensus.spawn(this)
            }
        }

        floorItemDespawn {
            if (ItemCensus.enabled) {
                ItemCensus.despawn(this)
            }
        }

        worldTimerStart("economy_census_snapshot") { TimeUnit.MINUTES.toTicks(Settings["economy.monitor.census.snapshotMinutes", 30]) }
        worldTimerTick("economy_census_snapshot") {
            if (!ItemCensus.enabled) {
                return@worldTimerTick Timer.CANCEL
            }
            snapshot()
            TimeUnit.MINUTES.toTicks(Settings["economy.monitor.census.snapshotMinutes", 30])
        }

        worldTimerStart("economy_census_reconcile") { TimeUnit.HOURS.toTicks(Settings["economy.monitor.census.reconcileHours", 6]) }
        worldTimerTick("economy_census_reconcile") {
            val hours = Settings["economy.monitor.census.reconcileHours", 6]
            if (!ItemCensus.enabled || hours <= 0) {
                return@worldTimerTick Timer.CANCEL
            }
            reconcile()
            TimeUnit.HOURS.toTicks(hours)
        }

        // Polls for the async reconciliation result so it is applied on the game thread
        worldTimerStart("economy_census_apply") { APPLY_POLL_TICKS }
        worldTimerTick("economy_census_apply") {
            val result = scanned.getAndSet(null)
            if (result != null) {
                ItemCensus.apply(result)
                return@worldTimerTick Timer.CANCEL
            }
            APPLY_POLL_TICKS
        }

        settingsReload {
            ItemCensus.reload()
            if (ItemCensus.enabled) {
                World.timers.start("economy_census_snapshot", restart = true)
                if (Settings["economy.monitor.census.reconcileHours", 6] > 0) {
                    World.timers.start("economy_census_reconcile", restart = true)
                }
            } else {
                World.timers.stop("economy_census_snapshot")
                World.timers.stop("economy_census_reconcile")
            }
        }

        worldTimerStop("economy_census_snapshot") { shutdown ->
            if (shutdown && ItemCensus.enabled) {
                CensusStore.save(ItemCensus.snapshot(offers))
            }
        }
    }

    private fun snapshot() {
        val counts = ItemCensus.snapshot(offers)
        for (count in counts) {
            if (count.total != 0L) {
                AuditLog.info("CENSUS\t${count.item}\tplayers=${count.players}\tfloor=${count.floor}\texchange=${count.exchange}\ttotal=${count.total}")
            }
        }
        CensusStore.save(counts)
        EconomySink.census(counts)
    }

    /**
     * Counts watched items across every account: online players from live
     * inventories on the game thread, offline accounts from saves on the IO
     * dispatcher. Logins or trades during the scan cause small false drift,
     * covered by the driftPercent tolerance.
     */
    private fun reconcile() {
        val watched = ItemCensus.watched().toSet()
        if (watched.isEmpty()) {
            return
        }
        val counts = Object2LongOpenHashMap<String>()
        val online = mutableSetOf<String>()
        for (player in Players) {
            online.add(player.accountName.lowercase())
            for ((id, inventory) in player.inventories.instances) {
                if (skip(id)) {
                    continue
                }
                for (item in inventory.items) {
                    count(counts, watched, item.id, item.amount)
                }
            }
        }
        scope.launch {
            for (definition in storage.names().values) {
                if (definition.accountName.lowercase() in online) {
                    continue
                }
                val save = storage.load(definition.accountName) ?: continue
                for ((id, items) in save.inventories) {
                    if (skip(id)) {
                        continue
                    }
                    for (item in items) {
                        count(counts, watched, item.id, item.amount)
                    }
                }
            }
            scanned.set(counts)
        }
        World.timers.start("economy_census_apply", restart = true)
    }

    private fun count(counts: Object2LongOpenHashMap<String>, watched: Set<String>, id: String, amount: Int) {
        if (id.isBlank() || amount <= 0) {
            return
        }
        val canonical = ItemCensus.canonical(id)
        if (canonical in watched) {
            counts.addTo(canonical, amount.toLong())
        }
    }

    private fun skip(inventory: String): Boolean = inventory.startsWith("_") || InventoryDefinitions.get(inventory)["shop", false]

    companion object {
        private const val APPLY_POLL_TICKS = 10
    }
}

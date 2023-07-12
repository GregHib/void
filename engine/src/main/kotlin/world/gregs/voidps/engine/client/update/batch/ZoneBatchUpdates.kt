package world.gregs.voidps.engine.client.update.batch

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import kotlinx.io.pool.DefaultPool
import kotlinx.io.pool.ObjectPool
import world.gregs.voidps.engine.client.update.view.Viewport
import world.gregs.voidps.engine.client.variable.getOrNull
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.map.zone.Zone
import world.gregs.voidps.network.encode.clearZone
import world.gregs.voidps.network.encode.encodeBatch
import world.gregs.voidps.network.encode.send
import world.gregs.voidps.network.encode.sendBatch
import world.gregs.voidps.network.encode.zone.ZoneUpdate

/**
 * Groups messages by [Zone] to send to all subscribed [Player]s
 * Batched messages are sent and cleared at the end of the tick
 * Initial messages are stored until removed and sent on subscription
 */
class ZoneBatchUpdates : Runnable {
    private val batches: MutableMap<Int, MutableList<ZoneUpdate>> = Int2ObjectOpenHashMap()
    private val encoded: MutableMap<Int, ByteArray> = Int2ObjectOpenHashMap()
    private val pool: ObjectPool<MutableList<ZoneUpdate>> = object : DefaultPool<MutableList<ZoneUpdate>>(INITIAL_UPDATE_POOL_SIZE) {
        override fun produceInstance() = ObjectArrayList<ZoneUpdate>()
        override fun clearInstance(instance: MutableList<ZoneUpdate>) = instance.apply { clear() }
    }
    private val senders = mutableListOf<Sender>()

    interface Sender {
        fun send(player: Player, zone: Zone)
    }

    fun register(sender: Sender) = senders.add(sender)

    /**
     * Adds [update] to the batch update for [zone]
     */
    fun add(zone: Zone, update: ZoneUpdate) {
        batches.getOrPut(zone.id) { ObjectArrayList() }.add(update)
    }

    override fun run() {
        for ((zone, updates) in batches) {
            encoded[zone] = encodeBatch(updates.filter { !it.private })
        }
    }

    fun run(player: Player) {
        val previousZone: Zone? = player.getOrNull("previous_zone")
        val previous = previousZone?.toRectangle(radius = player.viewport!!.localRadius)?.toZones(player.tile.level)?.toSet()
        player["previous_zone"] = player.tile.zone
        for (zone in player.tile.zone.toRectangle(radius = player.viewport!!.localRadius).toZones(player.tile.level)) {
            val entered = previous == null || !previous.contains(zone)
            if (entered) {
                player.clearZone(zone)
                for (sender in senders) {
                    sender.send(player, zone)
                }
            }
            val updates = batches[zone.id]?.filter { it.private && it.visible(player.name) } ?: continue
            if (!entered) {
                player.sendBatch(zone)
            }
            for (update in updates) {
                player.client?.send(update)
            }
        }
    }

    fun reset() {
        for (value in batches.values) {
            pool.recycle(value)
        }
        batches.clear()
    }

    private fun Player.sendBatch(zone: Zone) {
        val encoded = encoded[zone.id] ?: return
        val zoneOffset = getZoneOffset(viewport!!, zone)
        client?.sendBatch(encoded, zoneOffset.x, zoneOffset.y, zone.level)
    }

    companion object {
        private const val INITIAL_UPDATE_POOL_SIZE = 100

        /**
         * Returns the zone offset for [zone] relative to player's [viewport]
         */
        private fun getZoneOffset(viewport: Viewport, zone: Zone): Zone {
            val base = viewport.lastLoadZone.safeMinus(viewport.zoneRadius, viewport.zoneRadius)
            return zone.safeMinus(base)
        }

        private fun Player.clearZone(zone: Zone) {
            val zoneOffset = getZoneOffset(viewport!!, zone)
            client?.clearZone(zoneOffset.x, zoneOffset.y, zone.level)
        }
    }
}
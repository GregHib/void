package world.gregs.voidps.engine.client.update.batch

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import kotlinx.io.pool.DefaultPool
import kotlinx.io.pool.ObjectPool
import world.gregs.voidps.engine.client.update.view.Viewport
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.network.login.protocol.encode.clearZone
import world.gregs.voidps.network.login.protocol.encode.encodeBatch
import world.gregs.voidps.network.login.protocol.encode.send
import world.gregs.voidps.network.login.protocol.encode.sendBatch
import world.gregs.voidps.network.login.protocol.encode.zone.ZoneUpdate
import world.gregs.voidps.type.Zone

/**
 * Groups messages by [Zone] to send to all subscribed [Player]s
 * Batched messages are sent and cleared at the end of the tick
 * Initial messages are stored until removed and sent on subscription
 *
 * From experimenting with OSRS, it seems like this isn't quite accurate.
 * - Floor items are updated every change of zone.
 * - Objects are updated based on a 2-zone radius from the last updated zone.
 * (Like how region loading is a 4-zone radius)
 * - Projectiles are sent within a 3-zone radius but not sent when entering a new zone.
 * It's unclear where the other types fall.
 *
 * The types of batched messages would need to be split to replicate this behaviour.
 */
object ZoneBatchUpdates : Runnable {
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

    /**
     * Pre-encode batches of updates
     */
    override fun run() {
        for ((zone, updates) in batches) {
            encoded[zone] = encodeBatch(updates.filter { !it.private })
        }
    }

    /**
     * Also send updates each batch zone change
     */
    fun send(player: Player) {
        val viewport = player.viewport!!
        val from = viewport.lastBatchZone
        send(player, from)
        viewport.lastBatchZone = player.tile.zone
    }

    /**
     * Send updates each zone change
     */
    fun run(player: Player) {
        send(player, player.steps.previous.zone)
    }

    /**
     * Send differences between current and previous zones
     */
    private fun send(player: Player, from: Zone) {
        val viewport = player.viewport ?: return
        val to = player.tile.zone
        val previous = from.toRectangle(radius = viewport.localRadius).toZones(from.level).toSet()
        for (zone in to.toRectangle(radius = viewport.localRadius).toZonesReversed(player.tile.level)) {
            val entered = !previous.contains(zone)
            if (entered) { // Clear and resend raw data for newly entered areas
                player.clearZone(zone)
                for (sender in senders) {
                    sender.send(player, zone)
                }
            }
            val updates = batches[zone.id]?.filter { it.private && it.visible(player.name) } ?: continue
            if (!entered) { // Send batched updates for current regions
                player.sendBatch(zone)
            }
            for (update in updates) { // Send private updates separately
                player.client?.send(update)
            }
        }
    }

    fun clear() {
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

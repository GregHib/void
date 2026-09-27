package world.gregs.voidps.engine.map.instance

import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.clear
import world.gregs.voidps.engine.map.zone.DynamicZones
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Tile
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Allocates empty areas of the map for temporary use.
 *
 * Instances are freed automatically when empty of players for their given timeout,
 * checked every [CLEANUP_TICKS].
 */
object Instances : Runnable {

    private class Allocation(val key: Long, val timeout: Int, var lastSeen: Int)

    private var small: Deque<Region> = LinkedList()
    private var large: Deque<Region> = LinkedList()
    private val used = mutableMapOf<Region, Allocation>()
    private var lastKey = 0L
    private var lastSweep = -1

    init {
        reset()
    }

    /**
     * Allocates an empty 128x128 (2x2 region) area
     * @param timeout minutes to keep the instance after all players have left
     */
    fun small(timeout: Int = 0): Region = allocate(small.pollFirst(), timeout)

    /**
     * Allocates an empty 320x320 (5x5 region) area
     * @param timeout minutes to keep the instance after all players have left
     */
    fun large(timeout: Int = 0): Region = allocate(large.pollFirst(), timeout)

    private fun allocate(region: Region, timeout: Int): Region {
        lastKey = maxOf(lastKey + 1, System.currentTimeMillis())
        used[region] = Allocation(lastKey, TimeUnit.MINUTES.toTicks(timeout), GameLoop.tick)
        return region
    }

    fun isInstance(region: Region): Boolean = used.containsKey(region)

    fun reserved(region: Region): Boolean = region.x > FREE_REGION_X

    /**
     * Unique key of the current allocation of [instance], used to tell apart re-allocations of the same region
     */
    fun key(instance: Region): Long? = used[instance]?.key

    /**
     * Whether [instance] is still allocated under the same [key]
     */
    fun valid(instance: Region, key: Long?): Boolean = key != null && used[instance]?.key == key

    /**
     * The allocated instance which [region] is part of (including padding), or null
     */
    fun owner(region: Region): Region? {
        if (!reserved(region)) {
            return null
        }
        val size = if (region.y >= MID_POINT) LARGE_SIZE else SMALL_SIZE
        val startY = if (region.y >= MID_POINT) MID_POINT else FREE_REGION_Y
        val x = FREE_REGION_X + (region.x - FREE_REGION_X) / size * size + 1
        val y = startY + (region.y - startY) / size * size + 1
        val instance = Region(x, y)
        return if (used.containsKey(instance)) instance else null
    }

    fun owner(tile: Tile): Region? = owner(tile.region)

    /**
     * Whether a player was inside [instance] during the last clean-up pass
     */
    fun occupied(instance: Region): Boolean = used[instance]?.lastSeen == lastSweep

    /**
     * Frees [instance] for re-use and removes everything left inside it
     */
    private fun free(instance: Region) {
        if (used.remove(instance) == null) {
            return
        }
        clear(instance)
        if (instance.y >= MID_POINT) {
            large.add(instance)
        } else {
            small.add(instance)
        }
    }

    private fun clear(instance: Region) {
        get<DynamicZones>().clear(instance)
        for (level in 0..3) {
            NPCs.clear(instance.toLevel(level))
        }
        for (zone in instance.toCuboid().toZones()) {
            // Floor items too, or they'd linger and resurface when the instance is reused.
            for (item in FloorItems.at(zone).flatten()) {
                FloorItems.remove(item)
            }
            GameObjects.clear(zone)
            Collisions.clear(zone)
        }
    }

    override fun run() {
        if (GameLoop.tick % CLEANUP_TICKS != 0) {
            return
        }
        cleanup()
    }

    /**
     * Frees all instances which have been empty for longer than their timeout
     */
    fun cleanup() {
        val tick = GameLoop.tick
        lastSweep = tick
        for (player in Players) {
            val instance = owner(player.tile) ?: continue
            used[instance]?.lastSeen = tick
        }
        val expired = used.filterValues { tick - it.lastSeen > maxOf(it.timeout, CLEANUP_TICKS) }.keys
        for (instance in expired) {
            free(instance)
        }
    }

    fun reset() {
        used.clear()
        small.clear()
        large.clear()
        lastKey = 0L
        lastSweep = -1
        for (x in FREE_REGION_X until MAX_REGION - SMALL_SIZE step SMALL_SIZE) {
            for (y in FREE_REGION_Y until MID_POINT - SMALL_SIZE step SMALL_SIZE) {
                small.add(Region(x + 1, y + 1))
            }
        }
        for (x in FREE_REGION_X until MAX_REGION - LARGE_SIZE step LARGE_SIZE) {
            for (y in MID_POINT until MAX_REGION - LARGE_SIZE step LARGE_SIZE) {
                large.add(Region(x + 1, y + 1))
            }
        }
    }

    const val CLEANUP_TICKS = 50
    private const val SMALL_SIZE = 3
    private const val LARGE_SIZE = 6
    private const val FREE_REGION_X = 100
    private const val FREE_REGION_Y = 0
    private const val MAX_REGION = 255
    private const val MID_POINT = 82
}

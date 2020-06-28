package rs.dusk.engine.model.entity.obj

import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import rs.dusk.engine.model.world.ChunkPlane
import rs.dusk.engine.model.world.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since June 27, 2020
 */
class Objects(
    private val chunks: HashMap<ChunkPlane, MutableSet<Location>> = hashMapOf(),
    private val added: HashMap<ChunkPlane, MutableSet<Location>> = hashMapOf(),
    private val removed: HashMap<ChunkPlane, MutableSet<Location>> = hashMapOf(),
    private val timers: MutableMap<Location, Job> = mutableMapOf()
) {

    fun addTemp(location: Location) : Boolean {
        val id = location.tile.chunkPlane
        removed[id]?.remove(location)
        return added.getOrPut(id) { mutableSetOf() }.add(location)
    }

    fun removeTemp(location: Location): Boolean {
        val id = location.tile.chunkPlane
        added[id]?.remove(location)
        return removed.getOrPut(id) { mutableSetOf() }.add(location)
    }

    fun setTimer(location: Location, job: Job) {
        timers[location] = job
    }

    fun cancelTimer(location: Location): Boolean {
        val timer = timers[location] ?: return false
        timer.cancel("Cancelled by clear.")
        timers.remove(location)
        return true
    }

    fun add(location: Location) = chunks.getOrPut(location.tile.chunkPlane) { mutableSetOf() }.add(location)

    fun remove(location: Location): Boolean {
        val tile = chunks[location.tile.chunkPlane] ?: return false
        return tile.remove(location)
    }

    operator fun get(tile: Tile): Set<Location> {
        return get(tile.chunkPlane).filter { it.tile == tile }.toSet()
    }


    fun getType(tile: Tile, type: Int): Location? {
        return get(tile.chunkPlane).firstOrNull { it.type == type && it.tile == tile }
    }

    operator fun get(tile: Tile, id: Int): Location? {
        return get(tile.chunkPlane).firstOrNull { it.id == id && it.tile == tile }
    }

    operator fun get(chunkPlane: ChunkPlane): Set<Location> {
        val set = mutableSetOf<Location>()
        val base = chunks[chunkPlane]
        if(base != null) {
            set.addAll(base)
        }
        val removed = getRemoved(chunkPlane)
        if(removed != null) {
            set.removeAll(removed)
        }
        val added = getAdded(chunkPlane)
        if(added != null) {
            set.addAll(added)
        }
        return set
    }

    fun getAdded(chunkPlane: ChunkPlane): Set<Location>? = added[chunkPlane]

    fun getRemoved(chunkPlane: ChunkPlane): Set<Location>? = removed[chunkPlane]


}
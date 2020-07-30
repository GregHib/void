package rs.dusk.engine.model.entity.obj

import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import rs.dusk.engine.model.entity.list.BatchList
import rs.dusk.engine.model.map.Tile
import rs.dusk.engine.model.map.chunk.Chunk

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since June 27, 2020
 */
class Objects(
    override val chunks: HashMap<Chunk, MutableSet<GameObject>> = hashMapOf(),
    private val added: HashMap<Chunk, MutableSet<GameObject>> = hashMapOf(),
    private val removed: HashMap<Chunk, MutableSet<GameObject>> = hashMapOf(),
    private val timers: MutableMap<GameObject, Job> = mutableMapOf()
) : BatchList<GameObject> {

    fun addTemp(gameObject: GameObject) : Boolean {
        val id = gameObject.tile.chunk
        removed[id]?.remove(gameObject)
        return added.getOrPut(id) { mutableSetOf() }.add(gameObject)
    }

    fun removeTemp(gameObject: GameObject): Boolean {
        val id = gameObject.tile.chunk
        added[id]?.remove(gameObject)
        return removed.getOrPut(id) { mutableSetOf() }.add(gameObject)
    }

    override fun clear(chunk: Chunk) {
        super.clear(chunk)
        added.remove(chunk)
        removed.remove(chunk)
    }

    fun setTimer(gameObject: GameObject, job: Job) {
        timers[gameObject] = job
    }

    fun cancelTimer(gameObject: GameObject): Boolean {
        val timer = timers[gameObject] ?: return false
        timer.cancel("Cancelled by clear.")
        timers.remove(gameObject)
        return true
    }

    override operator fun get(tile: Tile): Set<GameObject> {
        return get(tile.chunk).filter { it.tile == tile }.toSet()
    }

    fun getType(tile: Tile, type: Int): GameObject? {
        return get(tile.chunk).firstOrNull { it.type == type && it.tile == tile }
    }

    operator fun get(tile: Tile, id: Int): GameObject? {
        return get(tile.chunk).firstOrNull { it.id == id && it.tile == tile }
    }

    override operator fun get(chunk: Chunk): Set<GameObject> {
        val set = mutableSetOf<GameObject>()
        val base = chunks[chunk]
        if(base != null) {
            set.addAll(base)
        }
        val removed = getRemoved(chunk)
        if(removed != null) {
            set.removeAll(removed)
        }
        val added = getAdded(chunk)
        if(added != null) {
            set.addAll(added)
        }
        return set
    }

    fun getStatic(chunk: Chunk): Set<GameObject>? = chunks[chunk]

    fun getAdded(chunk: Chunk): Set<GameObject>? = added[chunk]

    fun getRemoved(chunk: Chunk): Set<GameObject>? = removed[chunk]

}
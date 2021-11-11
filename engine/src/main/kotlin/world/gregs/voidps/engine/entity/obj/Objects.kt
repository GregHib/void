package world.gregs.voidps.engine.entity.obj

import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import world.gregs.voidps.engine.entity.list.BatchList
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.chunk.Chunk

class Objects(
    override val chunks: HashMap<Chunk, MutableList<GameObject>> = hashMapOf(),
    private val added: HashMap<Chunk, MutableSet<GameObject>> = hashMapOf(),
    private val removed: HashMap<Chunk, MutableSet<GameObject>> = hashMapOf(),
    private val timers: MutableMap<GameObject, Job> = mutableMapOf()
) : BatchList<GameObject> {

    fun addTemp(gameObject: GameObject): Boolean {
        return if (isOriginal(gameObject)) {
            removeRemoval(gameObject)
        } else {
            addAddition(gameObject)
        }
    }

    fun removeTemp(gameObject: GameObject): Boolean {
        return if (isOriginal(gameObject)) {
            addRemoval(gameObject)
        } else {
            removeAddition(gameObject)
        }
    }

    private fun addRemoval(gameObject: GameObject) = removed.getOrPut(gameObject.tile.chunk) { mutableSetOf() }.add(gameObject)

    private fun removeRemoval(gameObject: GameObject) = removed[gameObject.tile.chunk]?.remove(gameObject) ?: false

    private fun addAddition(gameObject: GameObject) = added.getOrPut(gameObject.tile.chunk) { mutableSetOf() }.add(gameObject)

    private fun removeAddition(gameObject: GameObject) = added[gameObject.tile.chunk]?.remove(gameObject) ?: false

    fun isOriginal(gameObject: GameObject) = chunks[gameObject.tile.chunk]?.contains(gameObject) ?: false

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

    override operator fun get(tile: Tile): List<GameObject> {
        return get(tile.chunk).filter { it.tile == tile }
    }

    fun getType(tile: Tile, type: Int): GameObject? {
        return get(tile.chunk).firstOrNull { it.type == type && it.tile == tile }
    }

    operator fun get(tile: Tile, id: Int): GameObject? {
        return get(tile.chunk).firstOrNull { it.def.id == id && it.tile == tile }
    }

    operator fun get(tile: Tile, id: String): GameObject? {
        return get(tile.chunk).firstOrNull { it.id == id && it.tile == tile }
    }

    override operator fun get(chunk: Chunk): List<GameObject> {
        val list = mutableListOf<GameObject>()
        val base = getStatic(chunk)
        if (base != null) {
            list.addAll(base)
        }
        val removed = getRemoved(chunk)
        if (removed != null) {
            list.removeAll(removed)
        }
        val added = getAdded(chunk)
        if (added != null) {
            list.addAll(added)
        }
        return list
    }

    fun getStatic(chunk: Chunk): List<GameObject>? = chunks[chunk]

    fun getAdded(chunk: Chunk): Set<GameObject>? = added[chunk]

    fun getRemoved(chunk: Chunk): Set<GameObject>? = removed[chunk]

}
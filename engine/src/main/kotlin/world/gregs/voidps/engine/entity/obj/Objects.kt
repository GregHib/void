package world.gregs.voidps.engine.entity.obj

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import world.gregs.voidps.engine.entity.BatchList
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.chunk.Chunk

class Objects(
    override val chunks: MutableMap<Int, MutableList<GameObject>> = Int2ObjectOpenHashMap(),
    private val added: MutableMap<Int, MutableSet<GameObject>> = Int2ObjectOpenHashMap(),
    private val removed: MutableMap<Int, MutableSet<GameObject>> = Int2ObjectOpenHashMap(),
    private val timers: MutableMap<GameObject, String> = mutableMapOf()
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

    private fun addRemoval(gameObject: GameObject) = removed.getOrPut(gameObject.tile.chunk.id) { mutableSetOf() }.add(gameObject)

    private fun removeRemoval(gameObject: GameObject) = removed[gameObject.tile.chunk.id]?.remove(gameObject) ?: false

    private fun addAddition(gameObject: GameObject) = added.getOrPut(gameObject.tile.chunk.id) { mutableSetOf() }.add(gameObject)

    private fun removeAddition(gameObject: GameObject) = added[gameObject.tile.chunk.id]?.remove(gameObject) ?: false

    fun isOriginal(gameObject: GameObject) = chunks[gameObject.tile.chunk.id]?.contains(gameObject) ?: false

    override fun clear(chunk: Chunk) {
        super.clear(chunk)
        added.remove(chunk.id)
        removed.remove(chunk.id)
    }

    fun setTimer(gameObject: GameObject, timer: String) {
        timers[gameObject] = timer
    }

    fun cancelTimer(gameObject: GameObject): Boolean {
        val timer = timers.remove(gameObject) ?: return false
        World.stopTimer(timer)
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

    operator fun get(chunk: Chunk, filter: (GameObject) -> Boolean): GameObject? {
        return get(chunk).firstOrNull(filter)
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

    fun getStatic(chunk: Chunk): List<GameObject>? = chunks[chunk.id]

    fun getAdded(chunk: Chunk): Set<GameObject>? = added[chunk.id]

    fun getRemoved(chunk: Chunk): Set<GameObject>? = removed[chunk.id]

    fun getAll(): Set<GameObject> {
        return added.values.flatten().union(removed.values.flatten())
    }

}
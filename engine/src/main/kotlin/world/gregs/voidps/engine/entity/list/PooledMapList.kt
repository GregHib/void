package world.gregs.voidps.engine.entity.list

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.chunk.Chunk
import java.util.*

/**
 * Spatial entity list can store both tile and chunk location
 */
interface PooledMapList<T : Character> {

    val indexed: Array<T?>
    val data: Int2ObjectOpenHashMap<ObjectLinkedOpenHashSet<T>>
    val pool: LinkedList<ObjectLinkedOpenHashSet<T>>

    val count: Int
        get() = indexed.count { it != null }

    fun add(entity: T) {
        add(entity.tile, entity)
        add(entity.tile.chunk, entity)
        addAtIndex(entity.index, entity)
    }

    fun remove(entity: T) {
        remove(entity.tile, entity)
        remove(entity.tile.chunk, entity)
        removeAtIndex(entity.index)
    }

    fun getAtIndex(index: Int): T? = indexed[index]

    fun addAtIndex(index: Int, entity: T) {
        indexed[index] = entity
    }

    fun removeAtIndex(index: Int) {
        indexed[index] = null
    }

    operator fun get(hash: Int): Set<T> = data.get(hash) ?: emptySet()

    fun add(hash: Int, entity: T): Boolean {
        val tile = data.getOrPut(hash) {
            if (pool.isNotEmpty()) {
                pool.poll()
            } else {
                ObjectLinkedOpenHashSet()
            }
        }
        return tile.add(entity)
    }

    fun remove(hash: Int, entity: T): Boolean {
        val tile = data.get(hash) ?: return false
        if (!tile.remove(entity)) {
            return false
        }

        if (tile.isEmpty()) {
            data.remove(hash)
            pool.add(tile)
        }

        return true
    }

    operator fun get(chunk: Chunk) = get(chunk.id or PLANE_OFFSET)

    operator fun get(tile: Tile) = get(tile.id)

    operator fun get(x: Int, y: Int, plane: Int = 0) = get(Tile(x, y, plane))

    fun add(chunk: Chunk, entity: T) = add(chunk.id or PLANE_OFFSET, entity)

    fun add(tile: Tile, entity: T) = add(tile.id, entity)

    fun add(x: Int, y: Int, plane: Int = 0, entity: T) = add(Tile(x, y, plane), entity)

    fun remove(chunk: Chunk, entity: T) = remove(chunk.id or PLANE_OFFSET, entity)

    fun remove(tile: Tile, entity: T) = remove(tile.id, entity)

    fun remove(x: Int, y: Int, plane: Int = 0, entity: T) = remove(Tile(x, y, plane), entity)

    operator fun set(hash: Int, entity: T) = add(hash, entity)

    operator fun set(tile: Tile, entity: T) = add(tile, entity)

    operator fun set(x: Int, y: Int, plane: Int = 0, entity: T) = add(x, y, plane, entity)

    fun update(from: Tile, to: Tile, entity: T) {
        remove(from, entity)
        remove(from.chunk, entity)
        add(to, entity)
        add(to.chunk, entity)
    }

    operator fun set(chunk: Chunk, entity: T) = add(chunk, entity)

    fun forEach(action: (T) -> Unit) = indexed.forEach {
        if (it != null) {
            action(it)
        }
    }

    fun clear() {
        forEach {
            remove(it)
        }
    }

    companion object {
        private const val PLANE_OFFSET = 0x40000000// Store chunk's on plane 4
    }
}
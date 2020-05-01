package rs.dusk.engine.entity.list

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import rs.dusk.engine.entity.model.Entity
import rs.dusk.engine.model.Chunk
import java.util.*

/**
 * Spatial entity list can store both tile and chunk location
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
interface PooledMapList<T : Entity> : EntityList<T> {

    val indexed: Array<T?>
    val data: Int2ObjectOpenHashMap<ObjectLinkedOpenHashSet<T>>
    val pool: LinkedList<ObjectLinkedOpenHashSet<T>>

    val count: Int
        get() = indexed.count { it != null }

    fun getAtIndex(index: Int): T? = indexed[index]

    fun addAtIndex(index: Int, entity: T) {
        indexed[index] = entity
    }

    override operator fun get(hash: Int): Set<T>? = data.get(hash)

    override fun add(hash: Int, entity: T): Boolean {
        val tile = data.getOrPut(hash) {
            if (pool.isNotEmpty()) {
                pool.poll()
            } else {
                ObjectLinkedOpenHashSet()
            }
        }
        return tile.add(entity)
    }

    override fun remove(hash: Int, entity: T): Boolean {
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

    operator fun get(chunk: Chunk) = get(chunk.id + PLANE_OFFSET)

    fun add(chunk: Chunk, entity: T) = add(chunk.id + PLANE_OFFSET, entity)

    fun remove(chunk: Chunk, entity: T) = remove(chunk.id + PLANE_OFFSET, entity)

    operator fun set(chunk: Chunk, entity: T) = add(chunk, entity)

    override fun forEach(action: (T) -> Unit) = indexed.forEach {
        if (it != null) {
            action(it)
        }
    }

    companion object {
        private const val PLANE_OFFSET = 0x40000000// Store chunk's on plane 4
    }
}
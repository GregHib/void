package rs.dusk.engine.entity.list

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import rs.dusk.engine.entity.model.Entity
import java.util.*

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
interface PooledList<T : Entity> : EntityList<T> {

    val delegate: Int2ObjectOpenHashMap<ObjectLinkedOpenHashSet<T>>
    val pool: LinkedList<ObjectLinkedOpenHashSet<T>>

    override operator fun get(hash: Int): Set<T>? = delegate.get(hash)

    override fun add(hash: Int, entity: T): Boolean {
        val tile = delegate.getOrPut(hash) {
            if (pool.isNotEmpty()) {
                pool.poll()
            } else {
                ObjectLinkedOpenHashSet()
            }
        }
        return tile.add(entity)
    }

    override fun remove(hash: Int, entity: T): Boolean {
        val tile = delegate.get(hash) ?: return false
        if (!tile.remove(entity)) {
            return false
        }

        if (tile.isEmpty()) {
            delegate.remove(hash)
            pool.add(tile)
        }

        return true
    }

    override fun forEach(action: (T) -> Unit) = delegate.forEach { (_, set) ->
        set.forEach(action)
    }
}
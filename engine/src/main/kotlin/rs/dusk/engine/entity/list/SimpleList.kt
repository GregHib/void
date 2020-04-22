package rs.dusk.engine.entity.list

import rs.dusk.engine.entity.model.Entity

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
interface SimpleList<T : Entity> : EntityList<T> {

    val delegate: HashMap<Int, MutableSet<T>>

    override operator fun get(hash: Int): Set<T>? = delegate[hash]

    override fun add(hash: Int, entity: T): Boolean {
        val tile = delegate.getOrPut(hash) { mutableSetOf() }
        return tile.add(entity)
    }

    override fun remove(hash: Int, entity: T): Boolean {
        val tile = delegate[hash] ?: return false
        return tile.remove(entity)
    }

    override fun forEach(action: (T) -> Unit) = delegate.forEach { (_, set) ->
        set.forEach(action)
    }
}
package rs.dusk.engine.model.entity.list

import rs.dusk.engine.model.entity.Entity
import rs.dusk.engine.model.world.ChunkPlane
import rs.dusk.engine.model.world.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since June 04, 2020
 */
interface BatchList<T : Entity> {

    val chunks: MutableMap<ChunkPlane, MutableSet<T>>

    fun add(entity: T) = chunks.getOrPut(entity.tile.chunkPlane) { mutableSetOf() }.add(entity)

    fun remove(entity: T): Boolean {
        val tile = chunks[entity.tile.chunkPlane] ?: return false
        return tile.remove(entity)
    }

    operator fun get(tile: Tile): Set<T> = get(tile.chunkPlane)

    operator fun get(chunkPlane: ChunkPlane): Set<T> = chunks[chunkPlane] ?: emptySet()
}
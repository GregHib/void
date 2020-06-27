package rs.dusk.engine.model.entity.obj

import rs.dusk.engine.model.world.ChunkPlane
import rs.dusk.engine.model.world.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since June 27, 2020
 */
class Objects {
    val chunks: HashMap<Int, MutableSet<Location>> = hashMapOf()

    fun add(location: Location) = chunks.getOrPut(location.tile.chunkPlane.id) { mutableSetOf() }.add(location)

    fun remove(location: Location): Boolean {
        val tile = chunks[location.tile.chunkPlane.id] ?: return false
        return tile.remove(location)
    }

    operator fun get(hash: Int): Set<Location>? = chunks[hash]

    operator fun get(x: Int, y: Int, plane: Int): Set<Location>? = get(ChunkPlane.getId(x / 8, y / 8, plane))

    operator fun get(chunkPlane: ChunkPlane): Set<Location>? = get(chunkPlane.id)

    operator fun get(tile: Tile): Set<Location>? = get(tile.chunkPlane)

}
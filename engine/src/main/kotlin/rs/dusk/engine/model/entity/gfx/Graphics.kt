package rs.dusk.engine.model.entity.gfx

import rs.dusk.engine.model.world.ChunkPlane
import rs.dusk.engine.model.world.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since July 04, 2020
 */
class Graphics {
    val chunks: MutableMap<Int, MutableSet<AreaGraphic>> = mutableMapOf()

    fun add(projectile: AreaGraphic) = chunks.getOrPut(projectile.tile.chunkPlane.id) { mutableSetOf() }.add(projectile)

    fun remove(projectile: AreaGraphic): Boolean {
        val tile = chunks[projectile.tile.chunkPlane.id] ?: return false
        return tile.remove(projectile)
    }

    operator fun get(tile: Tile) = get(tile.chunkPlane)

    operator fun get(chunkPlane: ChunkPlane) = chunks[chunkPlane.id]

}
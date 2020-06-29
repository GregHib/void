package rs.dusk.engine.model.entity.proj

import rs.dusk.engine.model.world.ChunkPlane
import rs.dusk.engine.model.world.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 30, 2020
 */
class Projectiles {
    val chunks: MutableMap<Int, MutableSet<Projectile>> = mutableMapOf()

    fun add(projectile: Projectile) = chunks.getOrPut(projectile.tile.chunkPlane.id) { mutableSetOf() }.add(projectile)

    fun remove(projectile: Projectile): Boolean {
        val tile = chunks[projectile.tile.chunkPlane.id] ?: return false
        return tile.remove(projectile)
    }

    operator fun get(tile: Tile) = get(tile.chunkPlane)

    operator fun get(chunkPlane: ChunkPlane) = chunks[chunkPlane.id]

}
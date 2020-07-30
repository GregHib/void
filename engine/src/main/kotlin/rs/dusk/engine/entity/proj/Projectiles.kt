package rs.dusk.engine.entity.proj

import rs.dusk.engine.entity.list.BatchList
import rs.dusk.engine.map.chunk.Chunk

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 30, 2020
 */
class Projectiles(override val chunks: MutableMap<Chunk, MutableSet<Projectile>> = mutableMapOf()) :
    BatchList<Projectile>
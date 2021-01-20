package world.gregs.void.engine.entity.proj

import world.gregs.void.engine.entity.list.BatchList
import world.gregs.void.engine.map.chunk.Chunk

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 30, 2020
 */
class Projectiles(override val chunks: MutableMap<Chunk, MutableSet<Projectile>> = mutableMapOf()) :
    BatchList<Projectile>
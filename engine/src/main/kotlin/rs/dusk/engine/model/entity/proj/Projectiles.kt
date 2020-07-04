package rs.dusk.engine.model.entity.proj

import rs.dusk.engine.model.entity.list.BatchList
import rs.dusk.engine.model.world.ChunkPlane

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 30, 2020
 */
class Projectiles(override val chunks: MutableMap<ChunkPlane, MutableSet<Projectile>> = mutableMapOf()) :
    BatchList<Projectile>
package world.gregs.voidps.engine.entity.proj

import world.gregs.voidps.engine.entity.list.BatchList
import world.gregs.voidps.engine.map.chunk.Chunk

/**
 * @author GregHib <greg@gregs.world>
 * @since March 30, 2020
 */
class Projectiles(override val chunks: MutableMap<Chunk, MutableSet<Projectile>> = mutableMapOf()) :
    BatchList<Projectile>
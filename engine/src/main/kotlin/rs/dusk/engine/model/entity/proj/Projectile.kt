package rs.dusk.engine.model.entity.proj

import rs.dusk.engine.model.entity.Entity
import rs.dusk.engine.model.world.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
data class Projectile(override val id: Int, override var tile: Tile) :
    Entity
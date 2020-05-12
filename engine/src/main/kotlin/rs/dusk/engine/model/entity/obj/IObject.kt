package rs.dusk.engine.model.entity.obj

import rs.dusk.engine.model.entity.Entity
import rs.dusk.engine.model.world.Tile

/**
 * Interactive Object
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
data class IObject(override val id: Int, override var tile: Tile) :
    Entity
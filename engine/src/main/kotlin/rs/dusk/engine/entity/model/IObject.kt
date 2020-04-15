package rs.dusk.engine.entity.model

import rs.dusk.engine.model.Tile

/**
 * Interactive Object
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
data class IObject(override val id: Int, override var tile: Tile) : Entity
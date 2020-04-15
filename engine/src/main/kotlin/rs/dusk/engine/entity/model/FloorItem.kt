package rs.dusk.engine.entity.model

import rs.dusk.engine.model.Tile

/**
 * An [Item] with physical location
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
data class FloorItem(override val id: Int, override var tile: Tile) : Entity
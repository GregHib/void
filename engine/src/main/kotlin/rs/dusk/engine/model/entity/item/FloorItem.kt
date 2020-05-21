package rs.dusk.engine.model.entity.item

import rs.dusk.engine.model.entity.Entity
import rs.dusk.engine.model.entity.Size
import rs.dusk.engine.model.world.Tile

/**
 * An [Item] with physical location
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
data class FloorItem(override val id: Int, override var tile: Tile, val size: Size = Size.TILE) : Entity
package rs.dusk.engine.model.entity.item

import rs.dusk.cache.definition.data.ItemDefinition
import rs.dusk.cache.definition.decoder.ItemDecoder
import rs.dusk.engine.model.entity.Entity
import rs.dusk.engine.model.entity.Size
import rs.dusk.engine.model.world.Tile
import rs.dusk.utility.get

/**
 * An [Item] with physical location
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
data class FloorItem(override val id: Int, override var tile: Tile, val size: Size = Size.TILE) : Entity {
    val def: ItemDefinition
        get() = get<ItemDecoder>().get(id)!!
}
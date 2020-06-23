package rs.dusk.engine.model.entity.item

import kotlinx.coroutines.Job
import rs.dusk.cache.definition.data.ItemDefinition
import rs.dusk.cache.definition.decoder.ItemDecoder
import rs.dusk.engine.model.entity.Entity
import rs.dusk.engine.model.entity.Size
import rs.dusk.engine.model.world.Tile
import rs.dusk.network.rs.codec.game.encode.message.FloorItemAddMessage
import rs.dusk.utility.get

/**
 * An [Item] with physical location
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
data class FloorItem(
    override var tile: Tile,
    override val id: Int,
    var amount: Int = 1,
    val size: Size = Size.TILE
) : Entity {

    val message = FloorItemAddMessage(tile.offset(), id, amount)

    val def: ItemDefinition
        get() = get<ItemDecoder>().get(id)!!

    var state: FloorItemState = FloorItemState.Private

    var disappear: Job? = null
}
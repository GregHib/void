package rs.dusk.engine.model.entity.item

import kotlinx.coroutines.Job
import rs.dusk.cache.definition.data.ItemDefinition
import rs.dusk.cache.definition.decoder.ItemDecoder
import rs.dusk.engine.model.entity.Entity
import rs.dusk.engine.model.entity.Size
import rs.dusk.engine.model.entity.character.player.Player
import rs.dusk.engine.model.entity.character.update.visual.player.name
import rs.dusk.engine.model.map.Tile
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
    val size: Size = Size.TILE,
    val owner: String? = null
) : Entity {

    fun visible(player: Player): Boolean {
        return owner == null || player.name == owner
    }

    val def: ItemDefinition
        get() = get<ItemDecoder>().getSafe(id)

    var state: FloorItemState = FloorItemState.Private

    var disappear: Job? = null
}
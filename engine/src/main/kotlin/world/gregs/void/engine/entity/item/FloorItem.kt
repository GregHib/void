package world.gregs.void.engine.entity.item

import kotlinx.coroutines.Job
import world.gregs.void.cache.definition.data.ItemDefinition
import world.gregs.void.engine.entity.Entity
import world.gregs.void.engine.entity.Size
import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.engine.entity.character.update.visual.player.name
import world.gregs.void.engine.entity.definition.ItemDefinitions
import world.gregs.void.engine.map.Tile
import world.gregs.void.engine.path.TargetStrategy
import world.gregs.void.utility.get

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
        get() = get<ItemDefinitions>().get(id)

    var state: FloorItemState = FloorItemState.Private

    var disappear: Job? = null

    lateinit var interactTarget: TargetStrategy
}
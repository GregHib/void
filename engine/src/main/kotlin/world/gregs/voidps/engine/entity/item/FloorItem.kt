package world.gregs.voidps.engine.entity.item

import kotlinx.coroutines.Job
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.player.name
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.path.strat.TileTargetStrategy
import world.gregs.voidps.utility.get

/**
 * An [Item] with physical location
 * @author GregHib <greg@gregs.world>
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

    lateinit var interactTarget: TileTargetStrategy
}
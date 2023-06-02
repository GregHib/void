package world.gregs.voidps.engine.entity.item.floor

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.data.definition.extra.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.EventHandlerStore
import world.gregs.voidps.engine.map.Tile

class FloorItemFactory(
    private val definitions: ItemDefinitions,
    private val store: EventHandlerStore
) {
    private val logger = InlineLogger()

    fun spawn(
        id: String,
        amount: Int,
        tile: Tile,
        revealTicks: Int = -1,
        disappearTicks: Int = -1,
        owner: Player? = null
    ): FloorItem {
        val definition = definitions.get(id)
        if (definitions.getOrNull(id) == null) {
            logger.warn { "Null floor item $id $tile" }
        }
        val item = FloorItem(id, tile, amount, revealTicks, disappearTicks, if (revealTicks == 0) 0 else owner?.index ?: 0)
        item.def = definition
        store.populate(item)
        return item
    }
}
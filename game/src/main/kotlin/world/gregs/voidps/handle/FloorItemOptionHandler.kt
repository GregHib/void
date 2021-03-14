package world.gregs.voidps.handle

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.player.face
import world.gregs.voidps.engine.entity.item.FloorItemOption
import world.gregs.voidps.engine.entity.item.FloorItems
import world.gregs.voidps.engine.event.EventBus
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.network.Handler
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.utility.inject

/**
 * @author GregHib <greg@gregs.world>
 * @since May 30, 2020
 */
class FloorItemOptionHandler : Handler() {

    val logger = InlineLogger()
    val items: FloorItems by inject()
    val bus: EventBus by inject()

    override fun floorItemOption(player: Player, id: Int, run: Boolean, y: Int, x: Int, optionIndex: Int) {
        val tile = Tile(x, y, player.tile.plane)
        val items = items[tile]
        val item = items.firstOrNull { it.id == id && it.tile == tile }
        if (item == null) {
            logger.warn { "Invalid floor item $id $tile" }
            return
        }
        val options = item.def.floorOptions
        if (optionIndex !in options.indices) {
            logger.warn { "Invalid floor item option $optionIndex ${options.contentToString()}" }
            return
        }
        val selectedOption = options[optionIndex]
        player.walkTo(item) { result ->
            player.face(item)
            if (result is PathResult.Failure) {
                player.message("You can't reach that.")
                return@walkTo
            }
            val partial = result is PathResult.Partial
            bus.emit(FloorItemOption(player, item, selectedOption, partial))
        }
    }

}
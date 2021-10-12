package world.gregs.voidps.engine.client.handle

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.player.face
import world.gregs.voidps.engine.entity.item.FloorItemClick
import world.gregs.voidps.engine.entity.item.FloorItemOption
import world.gregs.voidps.engine.entity.item.FloorItems
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.sync
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.network.Handler
import world.gregs.voidps.network.instruct.InteractFloorItem

class FloorItemOptionHandler : Handler<InteractFloorItem>() {

    private val items: FloorItems by inject()
    private val logger = InlineLogger()

    override fun validate(player: Player, instruction: InteractFloorItem) {
        val (id, x, y, optionIndex) = instruction
        val tile = player.tile.copy(x, y)
        val item = items[tile].firstOrNull { it.id == id && it.tile == tile }
        if (item == null) {
            logger.warn { "Invalid floor item $id $tile" }
            return
        }
        val options = item.def.floorOptions
        if (optionIndex !in options.indices) {
            logger.warn { "Invalid floor item option $optionIndex ${options.contentToString()}" }
            return
        }
        sync {
            val selectedOption = options[optionIndex]
            val click = FloorItemClick(item, selectedOption)
            player.events.emit(click)
            if (click.cancel) {
                return@sync
            }
            player.walkTo(item) {
                player.face(item)
                if (player.movement.result is PathResult.Failure) {
                    player.message("You can't reach that.")
                    return@walkTo
                }
                val partial = player.movement.result is PathResult.Partial
                player.events.emit(FloorItemOption(item, selectedOption, partial))
            }
        }
    }
}
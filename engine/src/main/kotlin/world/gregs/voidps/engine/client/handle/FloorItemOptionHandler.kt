package world.gregs.voidps.engine.client.handle

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.player.face
import world.gregs.voidps.engine.entity.item.FloorItemOption
import world.gregs.voidps.engine.entity.item.FloorItems
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.network.Handler
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.network.instruct.InteractFloorItem
import world.gregs.voidps.utility.inject

class FloorItemOptionHandler : Handler<InteractFloorItem>() {

    private val items: FloorItems by inject()
    private val logger = InlineLogger()

    override fun validate(player: Player, instruction: InteractFloorItem) {
        val (id, x, y, optionIndex) = instruction
        val tile = player.tile.copy(x, y)
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
            player.events.emit(FloorItemOption(item, selectedOption, partial))
        }
    }
}
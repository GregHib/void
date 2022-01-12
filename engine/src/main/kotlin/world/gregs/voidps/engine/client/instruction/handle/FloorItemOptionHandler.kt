package world.gregs.voidps.engine.client.instruction.handle

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.entity.character.move.interact
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.player.face
import world.gregs.voidps.engine.entity.item.FloorItemClick
import world.gregs.voidps.engine.entity.item.FloorItemOption
import world.gregs.voidps.engine.entity.item.FloorItems
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.network.instruct.InteractFloorItem

class FloorItemOptionHandler : InstructionHandler<InteractFloorItem>() {

    private val items: FloorItems by inject()
    private val logger = InlineLogger()

    override fun validate(player: Player, instruction: InteractFloorItem) {
        val (id, x, y, optionIndex) = instruction
        val tile = player.tile.copy(x, y)
        val item = items[tile].firstOrNull { it.def.id == id && it.tile == tile }
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
        val click = FloorItemClick(item, selectedOption)
        player.events.emit(click)
        if (click.cancelled) {
            return
        }
        player.walkTo(item) { path ->
            player.face(item)
            val partial = path.result is PathResult.Partial
            player.interact(FloorItemOption(item, selectedOption, partial))
        }
    }
}
package world.gregs.voidps.engine.client.instruction.handle

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItemClick
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.network.instruct.InteractFloorItem

class FloorItemOptionHandler(
    private val items: FloorItems
) : InstructionHandler<InteractFloorItem>() {

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
        player.events.emit(FloorItemClick(item, selectedOption))
    }
}
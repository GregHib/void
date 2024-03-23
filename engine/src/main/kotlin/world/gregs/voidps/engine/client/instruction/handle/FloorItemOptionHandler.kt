package world.gregs.voidps.engine.client.instruction.handle

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.item.floor.FloorItemOption
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.network.client.instruction.InteractFloorItem

class FloorItemOptionHandler(
    private val items: FloorItems
) : InstructionHandler<InteractFloorItem>() {

    private val logger = InlineLogger()

    override fun validate(player: Player, instruction: InteractFloorItem) {
        if (player.hasClock("delay") || player.hasClock("input_delay")) {
            return
        }
        val (id, x, y, optionIndex) = instruction
        val tile = player.tile.copy(x, y)
        val item = items[tile].firstOrNull { it.def.id == id }
        if (item == null) {
            logger.warn { "Invalid floor item $id $tile" }
            return
        }
        val options = item.def.floorOptions
        val selectedOption = options.getOrNull(optionIndex)
        if (selectedOption == null) {
            logger.warn { "Invalid floor item option $optionIndex ${options.contentToString()}" }
            return
        }
        if (selectedOption == "Examine") {
            player.message(item.def.getOrNull("examine") ?: return, ChatType.ItemExamine)
            return
        }
        player.mode = Interact(player, item, FloorItemOption(player, item, selectedOption), shape = -1)
    }
}
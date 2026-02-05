package world.gregs.voidps.engine.client.instruction.handle

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.entity.character.mode.interact.NPCOnFloorItemInteract
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnFloorItemInteract
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.network.client.instruction.InteractFloorItem

class FloorItemOptionHandler : InstructionHandler<InteractFloorItem>() {

    private val logger = InlineLogger()

    override fun validate(player: Player, instruction: InteractFloorItem): Boolean {
        if (player.contains("delay")) {
            return false
        }
        val (id, x, y, optionIndex) = instruction
        val tile = player.tile.copy(x, y)
        val floorItem = FloorItems.at(tile).firstOrNull { it.def.id == id }
        if (floorItem == null) {
            logger.warn { "Invalid floor item $id $tile" }
            return false
        }
        val options = floorItem.def.floorOptions
        val selectedOption = options.getOrNull(optionIndex)
        if (selectedOption == null) {
            logger.warn { "Invalid floor item option $optionIndex ${options.contentToString()}" }
            return false
        }
        if (selectedOption == "Examine") {
            player.message(floorItem.def.getOrNull("examine") ?: return false, ChatType.ItemExamine)
            return false
        }
        player.closeInterfaces()
        player.interactFloorItem(floorItem, selectedOption, -1)
        return true
    }
}

fun Player.interactFloorItem(target: FloorItem, option: String, shape: Int? = null) {
    mode = PlayerOnFloorItemInteract(target, option, this, shape)
}

fun NPC.interactFloorItem(target: FloorItem, option: String, shape: Int? = null) {
    mode = NPCOnFloorItemInteract(target, option, this, shape)
}

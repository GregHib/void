package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.network.client.instruction.ExamineItem

class ItemExamineHandler(
    private val definitions: ItemDefinitions
) : InstructionHandler<ExamineItem>() {

    override fun validate(player: Player, instruction: ExamineItem) {
        val definition = definitions.get(instruction.itemId)
        if (definition.contains("examine")) {
            player.message(definition["examine"], ChatType.Game)
        }
    }

}
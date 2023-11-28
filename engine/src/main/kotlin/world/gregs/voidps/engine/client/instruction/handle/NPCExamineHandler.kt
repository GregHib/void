package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.network.instruct.ExamineNpc

class NPCExamineHandler(
    private val definitions: NPCDefinitions
) : InstructionHandler<ExamineNpc>() {

    override fun validate(player: Player, instruction: ExamineNpc) {
        val definition = definitions.get(instruction.npcId)
        if (definition.contains("examine")) {
            player.message(definition["examine"], ChatType.Game)
        }
    }

}
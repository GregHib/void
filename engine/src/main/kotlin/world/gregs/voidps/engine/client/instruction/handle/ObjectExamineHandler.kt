package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.network.instruct.ExamineObject

class ObjectExamineHandler(
    private val definitions: ObjectDefinitions
) : InstructionHandler<ExamineObject>() {

    override fun validate(player: Player, instruction: ExamineObject) {
        val definition = definitions.get(instruction.objectId)
        if (definition.has("examine")) {
            player.message(definition["examine"], ChatType.Game)
        }
    }

}
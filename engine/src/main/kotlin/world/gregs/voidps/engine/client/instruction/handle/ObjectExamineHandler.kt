package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.definition.ObjectDefinitions
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.network.instruct.ExamineObject

class ObjectExamineHandler : InstructionHandler<ExamineObject>() {

    private val definitions: ObjectDefinitions by inject()

    override fun validate(player: Player, instruction: ExamineObject) {
        val definition = definitions.get(instruction.objectId)
        if (definition.has("examine")) {
            player.message(definition["examine"], ChatType.Game)
        }
    }

}
package world.gregs.voidps.engine.client.instruction.handle

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.ui.dialogue.Dialogues
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.client.instruction.InteractDialogue

class DialogueContinueHandler(
    private val definitions: InterfaceDefinitions,
) : InstructionHandler<InteractDialogue>() {

    private val logger = InlineLogger()

    override fun validate(player: Player, instruction: InteractDialogue): Boolean {
        val (interfaceId, componentId) = instruction
        val id = definitions.get(interfaceId).stringId
        if (!player.interfaces.contains(id)) {
            logger.debug { "Dialogue $interfaceId not found for player $player" }
            return false
        }

        val component = definitions.get(id).components?.get(componentId)
        if (component == null) {
            logger.debug { "Dialogue $interfaceId component $componentId not found for player $player" }
            return false
        }
        if (player["debug", false]) {
            logger.info { "$player - $id:${component.stringId}" }
        }
        Dialogues.continueDialogue(player, "$id:${component.stringId}")
        return true
    }
}

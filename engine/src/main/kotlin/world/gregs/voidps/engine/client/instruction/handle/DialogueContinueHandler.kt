package world.gregs.voidps.engine.client.instruction.handle

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.ui.dialogue.ContinueDialogue
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.definition.getComponentId
import world.gregs.voidps.network.instruct.InteractDialogue

class DialogueContinueHandler(
    private val definitions: InterfaceDefinitions
) : InstructionHandler<InteractDialogue>() {

    private val logger = InlineLogger()

    override fun validate(player: Player, instruction: InteractDialogue) {
        val (interfaceId, componentId, button) = instruction
        val id = definitions.get(interfaceId).stringId
        if (!player.interfaces.contains(id)) {
            logger.debug { "Dialogue $interfaceId not found for player $player" }
            return
        }

        val definition = definitions.get(id)
        val componentDefinition = definition.components?.get(componentId)
        if (componentDefinition == null) {
            logger.debug { "Dialogue $interfaceId component $componentId not found for player $player" }
            return
        }

        val component = definition.getComponentId(componentId)
        player.events.emit(
            ContinueDialogue(
                id,
                component,
                button
            )
        )
    }

}
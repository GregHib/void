package world.gregs.voidps.engine.client.instruction.handle

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.ui.dialogue.ContinueDialogue
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.definition.getComponentId
import world.gregs.voidps.engine.sync
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.network.instruct.InteractDialogue

class DialogueContinueHandler : InstructionHandler<InteractDialogue>() {

    private val definitions: InterfaceDefinitions by inject()
    private val logger = InlineLogger()

    override fun validate(player: Player, instruction: InteractDialogue) {
        val (interfaceId, componentId, button) = instruction
        if (!player.interfaces.contains(interfaceId)) {
            logger.debug { "Dialogue $interfaceId not found for player $player" }
            return
        }

        val id = definitions.getId(interfaceId)
        val definition = definitions.get(id)
        val componentDefinition = definition.components?.get(componentId)
        if (componentDefinition == null) {
            logger.debug { "Dialogue $interfaceId component $componentId not found for player $player" }
            return
        }

        val type = player.dialogues.currentType()
        if (type.isBlank()) {
            logger.debug { "Missing dialogue $interfaceId component $componentId option $componentId for player $player" }
            return
        }

        val component = definition.getComponentId(componentId)

        sync {
            player.events.emit(
                ContinueDialogue(
                    id,
                    component,
                    type,
                    button
                )
            )
        }
    }

}
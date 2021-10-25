package world.gregs.voidps.engine.client.instruction.handle

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.ui.dialogue.ContinueDialogue
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.definition.getComponentName
import world.gregs.voidps.engine.sync
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.network.instruct.InteractDialogue

class DialogueContinueHandler : InstructionHandler<InteractDialogue>() {

    private val definitions: InterfaceDefinitions by inject()
    private val logger = InlineLogger()

    override fun validate(player: Player, instruction: InteractDialogue) {
        val (id, componentId, button) = instruction
        if (!player.interfaces.contains(id)) {
            logger.debug { "Dialogue $id not found for player $player" }
            return
        }

        val definition = definitions.get(id)
        val component = definition.components?.get(componentId)
        if (component == null) {
            logger.debug { "Dialogue $id component $componentId not found for player $player" }
            return
        }

        val type = player.dialogues.currentType()
        if (type.isBlank()) {
            logger.debug { "Missing dialogue $id component $componentId option $componentId for player $player" }
            return
        }

        val name = definitions.getName(id)
        val componentName = definition.getComponentName(componentId)

        sync {
            player.events.emit(
                ContinueDialogue(
                    name,
                    componentName,
                    type,
                    button
                )
            )
        }
    }

}
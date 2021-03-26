package world.gregs.voidps.engine.client.handle

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.cache.definition.decoder.InterfaceDecoder
import world.gregs.voidps.engine.client.ui.detail.InterfaceDetails
import world.gregs.voidps.engine.client.ui.dialogue.ContinueDialogue
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Handler
import world.gregs.voidps.network.instruct.InteractDialogue
import world.gregs.voidps.utility.inject

class DialogueContinueHandler : Handler<InteractDialogue>() {

    private val lookup: InterfaceDetails by inject()
    private val decoder: InterfaceDecoder by inject()
    private val logger = InlineLogger()

    override fun validate(player: Player, instruction: InteractDialogue) {
        val (id, componentId, button) = instruction
        if (!player.interfaces.contains(id)) {
            logger.debug { "Dialogue $id not found for player $player" }
            return
        }

        val definition = decoder.get(id)
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

        val inter = lookup.get(id)
        val name = inter.name
        val componentName = inter.getComponentName(componentId)

        player.events.emit(
            ContinueDialogue(
                id,
                name,
                componentId,
                componentName,
                type,
                button
            )
        )
    }

}
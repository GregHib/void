package world.gregs.voidps.network.handle

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.cache.definition.decoder.InterfaceDecoder
import world.gregs.voidps.engine.client.ui.detail.InterfaceDetails
import world.gregs.voidps.engine.client.ui.dialogue.ContinueDialogue
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Handler
import world.gregs.voidps.utility.inject

/**
 * @author GregHib <greg@gregs.world>
 * @since April 30, 2020
 */
class DialogueContinueHandler : Handler() {

    val lookup: InterfaceDetails by inject()
    val decoder: InterfaceDecoder by inject()
    val logger = InlineLogger()

    override fun continueDialogue(player: Player, hash: Int, button: Int) {
        val id = hash shr 16
        val componentId = hash and 0xffff

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
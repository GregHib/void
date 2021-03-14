package world.gregs.voidps.handle

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.cache.definition.decoder.InterfaceDecoder
import world.gregs.voidps.engine.client.ui.detail.InterfaceDetails
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.EventBus
import world.gregs.voidps.engine.sync
import world.gregs.voidps.network.Handler
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.dialogue.event.ContinueDialogue

/**
 * @author GregHib <greg@gregs.world>
 * @since April 30, 2020
 */
class DialogueContinueHandler : Handler() {

    val bus: EventBus by inject()
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
        if(type.isBlank()) {
            logger.debug { "Missing dialogue $id component $componentId option $componentId for player $player" }
            return
        }

        val inter = lookup.get(id)
        val name = inter.name
        val componentName = inter.getComponentName(componentId)

        sync {
            bus.emit(
                ContinueDialogue(
                    player,
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

}
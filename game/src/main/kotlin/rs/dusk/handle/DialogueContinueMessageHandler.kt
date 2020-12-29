package rs.dusk.handle

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import rs.dusk.cache.definition.decoder.InterfaceDecoder
import rs.dusk.core.network.codec.message.MessageHandler
import rs.dusk.core.network.connection.getSession
import rs.dusk.engine.client.Sessions
import rs.dusk.engine.client.ui.detail.InterfaceDetails
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.task.TaskExecutor
import rs.dusk.engine.task.sync
import rs.dusk.utility.inject
import rs.dusk.world.interact.dialogue.event.ContinueDialogue

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 30, 2020
 */
class DialogueContinueMessageHandler : MessageHandler() {

    val sessions: Sessions by inject()
    val bus: EventBus by inject()
    val lookup: InterfaceDetails by inject()
    val executor: TaskExecutor by inject()
    val decoder: InterfaceDecoder by inject()
    val logger = InlineLogger()

    override fun continueDialogue(context: ChannelHandlerContext, hash: Int, button: Int) {
        val session = context.channel().getSession()
        val player = sessions.get(session) ?: return
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

        executor.sync {
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
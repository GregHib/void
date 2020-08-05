package rs.dusk.handle

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import rs.dusk.cache.definition.decoder.InterfaceDecoder
import rs.dusk.core.network.model.session.getSession
import rs.dusk.engine.client.Sessions
import rs.dusk.engine.client.ui.InterfacesLookup
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.task.TaskExecutor
import rs.dusk.engine.task.start
import rs.dusk.network.rs.codec.game.GameMessageHandler
import rs.dusk.network.rs.codec.game.decode.message.DialogueContinueMessage
import rs.dusk.utility.inject
import rs.dusk.world.interact.dialogue.event.ContinueDialogue

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 30, 2020
 */
class DialogueContinueMessageHandler : GameMessageHandler<DialogueContinueMessage>() {

    val sessions: Sessions by inject()
    val bus: EventBus by inject()
    val lookup: InterfacesLookup by inject()
    val executor: TaskExecutor by inject()
    val decoder: InterfaceDecoder by inject()
    val logger = InlineLogger()

    override fun handle(ctx: ChannelHandlerContext, msg: DialogueContinueMessage) {
        val session = ctx.channel().getSession()
        val player = sessions.get(session) ?: return
        val (hash, button) = msg
        val id = hash shr 16
        val componentId = hash and 0xffff

        if (!player.interfaces.contains(id)) {
            logger.debug { "Dialogue $id not found for player $player" }
            return
        }

        val definition = decoder.getSafe(id)
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
        val componentName = inter.components[componentId] ?: ""

        executor.start {
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
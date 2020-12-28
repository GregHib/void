package rs.dusk.handle

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import rs.dusk.cache.definition.decoder.InterfaceDecoder
import rs.dusk.core.network.codec.message.MessageHandler
import rs.dusk.core.network.model.session.getSession
import rs.dusk.engine.client.Sessions
import rs.dusk.engine.client.ui.detail.InterfaceDetails
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.task.TaskExecutor
import rs.dusk.engine.task.sync
import rs.dusk.utility.inject
import rs.dusk.world.interact.entity.player.display.InterfaceSwitch

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since July 31, 2020
 */
class InterfaceSwitchMessageHandler : MessageHandler() {

    val sessions: Sessions by inject()
    val bus: EventBus by inject()
    val executor: TaskExecutor by inject()
    val decoder: InterfaceDecoder by inject()
    val lookup: InterfaceDetails by inject()
    private val logger = InlineLogger()

    override fun interfaceSwitch(context: ChannelHandlerContext, toType: Int, fromSlot: Int, fromType: Int, fromHash: Int, toSlot: Int, toHash: Int) {
        val session = context.channel().getSession()
        val player = sessions.get(session) ?: return

        val fromId = fromHash shr 16
        if (!player.interfaces.contains(fromId)) {
            logger.debug { "Interface $fromId not found for player $player" }
            return
        }

        val fromComponentId = fromHash and 0xffff
        val fromDefinition = decoder.get(fromId)
        val fromComponent = fromDefinition.components?.get(fromComponentId)
        if (fromComponent == null) {
            logger.debug { "Interface $fromId component $fromComponentId not found for player $player" }
            return
        }

        val fromInter = lookup.get(fromId)
        val fromName = fromInter.name
        val fromComponentName = fromInter.getComponentName(fromComponentId)

        val toId = toHash shr 16
        if (!player.interfaces.contains(toId)) {
            logger.debug { "Interface $toId not found for player $player" }
            return
        }

        val toComponentId = toHash and 0xffff
        val toDefinition = decoder.get(toId)
        val toComponent = toDefinition.components?.get(toComponentId)
        if (toComponent == null) {
            logger.debug { "Interface $toId component $toComponentId not found for player $player" }
            return
        }

        val toInter = lookup.get(toId)
        val toName = toInter.name
        val toComponentName = toInter.getComponentName(toComponentId)


        executor.sync {
            bus.emit(
                InterfaceSwitch(
                    player,
                    fromId,
                    fromName,
                    fromComponentId,
                    fromComponentName,
                    fromType,
                    fromSlot,
                    toId,
                    toName,
                    toComponentId,
                    toComponentName,
                    toType,
                    toSlot
                )
            )
        }
    }

}
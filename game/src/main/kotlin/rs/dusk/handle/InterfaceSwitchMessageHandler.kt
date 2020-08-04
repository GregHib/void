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
import rs.dusk.network.rs.codec.game.decode.message.InterfaceSwitchComponentsMessage
import rs.dusk.utility.inject
import rs.dusk.world.interact.entity.player.display.InterfaceSwitch

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since July 31, 2020
 */
class InterfaceSwitchMessageHandler : GameMessageHandler<InterfaceSwitchComponentsMessage>() {

    val sessions: Sessions by inject()
    val bus: EventBus by inject()
    val executor: TaskExecutor by inject()
    val decoder: InterfaceDecoder by inject()
    val lookup: InterfacesLookup by inject()
    private val logger = InlineLogger()

    override fun handle(ctx: ChannelHandlerContext, msg: InterfaceSwitchComponentsMessage) {
        val session = ctx.channel().getSession()
        val player = sessions.get(session) ?: return
        val (fromType, fromSlot, toType, fromHash, toSlot, toHash) = msg

        val fromId = fromHash shr 16
        if (!player.interfaces.contains(fromId)) {
            logger.warn { "Interface $fromId not found for player $player" }
            return
        }

        val fromComponentId = fromHash and 0xffff
        val fromDefinition = decoder.getSafe(fromId)
        val fromComponent = fromDefinition.components?.get(fromComponentId)
        if (fromComponent == null) {
            logger.warn { "Interface $fromId component $fromComponentId not found for player $player" }
            return
        }

        val fromInter = lookup.get(fromId)
        val fromName = fromInter.name
        val fromComponentName = fromInter.components[fromComponentId] ?: ""

        val toId = toHash shr 16
        if (!player.interfaces.contains(toId)) {
            logger.warn { "Interface $toId not found for player $player" }
            return
        }

        val toComponentId = toHash and 0xffff
        val toDefinition = decoder.getSafe(toId)
        val toComponent = toDefinition.components?.get(toComponentId)
        if (toComponent == null) {
            logger.warn { "Interface $toId component $toComponentId not found for player $player" }
            return
        }

        val toInter = lookup.get(toId)
        val toName = toInter.name
        val toComponentName = toInter.components[toComponentId] ?: ""


        executor.start {
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
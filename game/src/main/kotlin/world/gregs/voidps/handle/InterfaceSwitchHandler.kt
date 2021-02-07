package world.gregs.voidps.handle

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.cache.definition.decoder.InterfaceDecoder
import world.gregs.voidps.engine.client.Sessions
import world.gregs.voidps.engine.client.ui.detail.InterfaceDetails
import world.gregs.voidps.engine.event.EventBus
import world.gregs.voidps.engine.sync
import world.gregs.voidps.network.codec.Handler
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.entity.player.display.InterfaceSwitch

/**
 * @author GregHib <greg@gregs.world>
 * @since July 31, 2020
 */
class InterfaceSwitchHandler : Handler() {

    val sessions: Sessions by inject()
    val bus: EventBus by inject()
    val decoder: InterfaceDecoder by inject()
    val lookup: InterfaceDetails by inject()
    private val logger = InlineLogger()

    override fun interfaceSwitch(context: ChannelHandlerContext, toType: Int, fromSlot: Int, fromType: Int, fromHash: Int, toSlot: Int, toHash: Int) {
        val session = context.channel()
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


        sync {
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
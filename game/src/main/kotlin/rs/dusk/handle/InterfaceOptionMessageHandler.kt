package rs.dusk.handle

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import rs.dusk.cache.definition.decoder.InterfaceDecoder
import rs.dusk.core.network.model.session.getSession
import rs.dusk.engine.client.Sessions
import rs.dusk.engine.client.ui.detail.InterfaceDetails
import rs.dusk.engine.entity.character.contain.container
import rs.dusk.engine.entity.character.contain.detail.ContainerDetails
import rs.dusk.engine.entity.item.detail.ItemDetails
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.task.TaskExecutor
import rs.dusk.engine.task.start
import rs.dusk.network.rs.codec.game.GameMessageHandler
import rs.dusk.network.rs.codec.game.decode.message.InterfaceOptionMessage
import rs.dusk.utility.inject
import rs.dusk.world.interact.entity.player.display.InterfaceOption

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since July 26, 2020
 */
class InterfaceOptionMessageHandler : GameMessageHandler<InterfaceOptionMessage>() {

    val sessions: Sessions by inject()
    val bus: EventBus by inject()
    val executor: TaskExecutor by inject()
    val decoder: InterfaceDecoder by inject()
    val interfaceDetails: InterfaceDetails by inject()
    val containerDetails: ContainerDetails by inject()
    val itemDetails: ItemDetails by inject()
    val logger = InlineLogger()

    override fun handle(ctx: ChannelHandlerContext, msg: InterfaceOptionMessage) {
        val session = ctx.channel().getSession()
        val player = sessions.get(session) ?: return
        val (hash, itemId, itemSlot, option) = msg

        val id = hash shr 16
        if (!player.interfaces.contains(id)) {
            logger.info { "Interface $id not found for player $player" }
            return
        }

        val componentId = hash and 0xffff
        val definition = decoder.get(id)
        val component = definition.components?.get(componentId)
        if(component == null) {
            logger.info { "Interface $id component $componentId not found for player $player" }
            return
        }

        val options = component.options
        if (options != null && option !in options.indices) {
            logger.info { "Interface $id component $componentId option $option not found for player $player ${options.toList()}" }
            return
        }

        val inter = interfaceDetails.get(id)
        val name = inter.name
        var item = ""

        if(itemId != -1 && itemSlot != -1) {
            val containerId = containerDetails.getId(name)
            val container = containerDetails.get(containerId)
            if(!player.container(container).isValidId(itemSlot, itemId)) {
                logger.info { "Interface $name item $itemId $itemSlot not found in $containerId for player $player" }
                return
            }
            item = itemDetails.getName(itemId)
        }

        val selectedOption = options?.getOrNull(option) ?: ""
        val componentName = inter.components[componentId] ?: ""
        executor.start {
            bus.emit(
                InterfaceOption(
                    player,
                    id,
                    name,
                    componentId,
                    componentName,
                    option,
                    selectedOption,
                    item,
                    itemId,
                    itemSlot
                )
            )
        }
    }

}
package rs.dusk.handle

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import rs.dusk.cache.config.decoder.ItemContainerDecoder
import rs.dusk.cache.definition.decoder.InterfaceDecoder
import rs.dusk.core.network.model.session.getSession
import rs.dusk.engine.client.Sessions
import rs.dusk.engine.client.ui.detail.InterfaceDetails
import rs.dusk.engine.entity.character.contain.container
import rs.dusk.engine.entity.character.contain.detail.ContainerDetails
import rs.dusk.engine.entity.character.contain.hasContainer
import rs.dusk.engine.entity.item.detail.ItemDetails
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.task.TaskExecutor
import rs.dusk.engine.task.sync
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
    val containerDecoder: ItemContainerDecoder by inject()
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
            val containers = component.containers
            if(containers == null) {
                logger.info { "Interface $name container not found for $itemId $itemSlot $player" }
                return
            }
            var found = false
            for(containerId in containers) {
                val def = containerDecoder.get(containerId)
                if(itemSlot > def.length) {
                    continue
                }

                if(!player.hasContainer(containerId)) {
                    continue
                }
                val containerDetails = containerDetails.get(containerId)

                val primary = player.container(containerDetails, secondary = false)
                if(primary.isValidId(itemSlot, itemId)) {
                    item = itemDetails.getName(itemId)
                    found = true
                    break
                }
                val secondary = player.container(containerDetails, secondary = true)
                if(secondary.isValidId(itemSlot, itemId)) {
                    item = itemDetails.getName(itemId)
                    found = true
                    break
                }
            }
            if(!found) {
                logger.info { "Interface $name container item $itemId $itemSlot not found for player $player" }
                return
            }
        }

        val selectedOption = options?.getOrNull(option) ?: ""
        val componentName = inter.components[componentId] ?: ""
        executor.sync {
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
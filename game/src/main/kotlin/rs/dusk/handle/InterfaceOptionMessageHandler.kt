package rs.dusk.handle

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import rs.dusk.cache.definition.decoder.InterfaceDecoder
import rs.dusk.core.network.model.session.getSession
import rs.dusk.engine.client.Sessions
import rs.dusk.engine.client.ui.detail.InterfaceDetails
import rs.dusk.engine.entity.character.contain.container
import rs.dusk.engine.entity.character.contain.hasContainer
import rs.dusk.engine.entity.definition.ContainerDefinitions
import rs.dusk.engine.entity.definition.ItemDefinitions
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
    val containerDefinitions: ContainerDefinitions by inject()
    val itemDefinitions: ItemDefinitions by inject()
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
        val componentDef = definition.components?.get(componentId)
        if (componentDef == null) {
            logger.info { "Interface $id component $componentId not found for player $player" }
            return
        }

        var options = componentDef.options

        val inter = interfaceDetails.get(id)
        val componentName = inter.getComponentName(componentId)
        val component = inter.getComponent(componentName)
        val name = inter.name
        var item = ""

        if (itemId != -1 && itemSlot != -1) {
            if (component == null) {
                logger.info { "Interface $name component $componentId not found for player $player" }
                return
            }
            val containerName = component.container
            if (!player.hasContainer(containerName)) {
                logger.info { "Interface $name container $containerName not found for player $player" }
                return
            }

            val def = containerDefinitions.get(containerName)
            if (itemSlot > def.length) {
                logger.info { "Invalid interface $name container $containerName ${def.length} slot $itemSlot not found for player $player" }
                return
            }

            var found = false
            val primary = player.container(def, secondary = false)
            if (primary.isValidId(itemSlot, itemId)) {
                item = itemDefinitions.getName(itemId)
                found = true
            } else {
                val secondary = player.container(def, secondary = true)
                if (secondary.isValidId(itemSlot, itemId)) {
                    item = itemDefinitions.getName(itemId)
                    found = true
                }
            }
            if (!found) {
                logger.info { "Interface $name container item $itemId $itemSlot not found for player $player" }
                return
            }
        }
        if(options == null) {
            options = player.interfaceOptions.get(name, componentName)
        }

        if (option !in options.indices) {
            logger.info { "Interface $id component $componentId option $option not found for player $player ${options.toList()}" }
            return
        }

        val selectedOption = options.getOrNull(option) ?: ""
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
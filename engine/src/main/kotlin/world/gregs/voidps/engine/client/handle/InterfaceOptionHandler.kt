package world.gregs.voidps.engine.client.handle

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.cache.definition.decoder.InterfaceDecoder
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.entity.character.contain.container
import world.gregs.voidps.engine.entity.character.contain.hasContainer
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.*
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.network.Handler
import world.gregs.voidps.network.instruct.InteractInterface
import world.gregs.voidps.utility.inject

class InterfaceOptionHandler : Handler<InteractInterface>() {

    private val decoder: InterfaceDecoder by inject()
    private val interfaceDefinitions: InterfaceDefinitions by inject()
    private val containerDefinitions: ContainerDefinitions by inject()
    private val itemDefinitions: ItemDefinitions by inject()
    private val logger = InlineLogger()

    override fun validate(player: Player, instruction: InteractInterface) {
        val (id, componentId, itemId, itemSlot, option) = instruction

        if (!player.interfaces.contains(interfaceDefinitions.getName(id))) {
            logger.info { "Interface $id not found for player $player" }
            return
        }
        val definition = decoder.get(id)
        val componentDef = definition.components?.get(componentId)
        if (componentDef == null) {
            logger.info { "Interface $id component $componentId not found for player $player" }
            return
        }

        var options = componentDef.options

        val inter = interfaceDefinitions.get(id)
        val componentName = inter.getComponentName(componentId)
        val component = inter.getComponentOrNull(componentName)
        val name = interfaceDefinitions.getName(id)

        var item = Item.EMPTY
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
            val itemName = itemDefinitions.getName(itemId)
            if (primary.isValidId(itemSlot, itemName)) {
                found = true
                item = primary.getItem(itemSlot)
            } else {
                val secondary = player.container(def, secondary = true)
                if (secondary.isValidId(itemSlot, itemName)) {
                    found = true
                    item = secondary.getItem(itemSlot)
                }
            }
            if (!found) {
                logger.info { "Interface $name container item $item $itemSlot not found for player $player" }
                return
            }
        }
        if (options == null) {
            options = player.interfaceOptions.get(name, componentName)
        }

        if (option !in options.indices) {
            logger.info { "Interface $id component $componentId option $option not found for player $player ${options.toList()}" }
            return
        }

        val selectedOption = options.getOrNull(option) ?: ""
        player.events.emit(
            InterfaceOption(
                id,
                name,
                componentId,
                componentName,
                option,
                selectedOption,
                item,
                itemSlot
            )
        )
    }

}
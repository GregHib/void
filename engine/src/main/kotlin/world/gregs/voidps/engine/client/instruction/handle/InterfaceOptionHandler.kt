package world.gregs.voidps.engine.client.instruction.handle

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.ui.InterfaceClick
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.entity.character.contain.container
import world.gregs.voidps.engine.entity.character.contain.equipment
import world.gregs.voidps.engine.entity.character.contain.hasContainer
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.ContainerDefinitions
import world.gregs.voidps.engine.entity.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.definition.getComponentId
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.sync
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.network.instruct.InteractInterface

class InterfaceOptionHandler : InstructionHandler<InteractInterface>() {

    private val interfaceDefinitions: InterfaceDefinitions by inject()
    private val containerDefinitions: ContainerDefinitions by inject()
    private val itemDefinitions: ItemDefinitions by inject()
    private val logger = InlineLogger()

    override fun validate(player: Player, instruction: InteractInterface) {
        var (interfaceId, componentId, itemId, itemSlot, option) = instruction

        val id = interfaceDefinitions.get(interfaceId).stringId
        if (!player.interfaces.contains(id)) {
            logger.info { "Interface $interfaceId not found for player $player" }
            return
        }
        val definition = interfaceDefinitions.get(id)
        val component = definition.getComponentId(componentId)
        val componentDefinition = definition.components?.get(componentId)
        if (componentDefinition == null) {
            logger.info { "Interface $interfaceId component $componentId not found for player $player" }
            return
        }

        var options = componentDefinition.options

        var item = Item.EMPTY
        if (itemId != -1) {
            if (component.isEmpty()) {
                logger.info { "Interface $id component $componentId not found for player $player" }
                return
            }
            val containerName = componentDefinition["container", ""]
            if (!player.hasContainer(containerName)) {
                logger.info { "Interface $id container $containerName not found for player $player" }
                return
            }

            val def = containerDefinitions.get(containerName)
            if (itemSlot > def.length) {
                logger.info { "Invalid interface $id container $containerName ${def.length} slot $itemSlot not found for player $player" }
                return
            }

            val itemName = itemDefinitions.get(itemId).stringId
            if (itemSlot == -1 && containerName == "worn_equipment") {
                itemSlot = player.equipment.indexOf(itemName)
            } else if (itemSlot == -1 && containerName == "item_loan") {
                itemSlot = 0
            }
            val secondary = !componentDefinition["primary", true]
            val container = player.container(def, secondary = secondary)
            if (!container.isValidId(itemSlot, itemName)) {
                logger.info { "Interface $id container item $item $itemSlot not found for player $player" }
                return
            }
            item = container.getItem(itemSlot)
        }
        if (options == null) {
            options = player.interfaceOptions.get(id, component)
        }

        if (option !in options.indices) {
            logger.info { "Interface $interfaceId component $componentId option $option not found for player $player ${options.toList()}" }
            return
        }

        val selectedOption = options.getOrNull(option) ?: ""
        sync {
            val click = InterfaceClick(
                id,
                component,
                option,
                selectedOption,
                item,
                itemSlot
            )
            player.events.emit(click)
            if (click.cancel) {
                return@sync
            }
            player.events.emit(
                InterfaceOption(
                    id,
                    component,
                    option,
                    selectedOption,
                    item,
                    itemSlot
                )
            )
        }
    }

}
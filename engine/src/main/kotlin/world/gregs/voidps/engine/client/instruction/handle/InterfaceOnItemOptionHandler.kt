package world.gregs.voidps.engine.client.instruction.handle

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnItem
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
import world.gregs.voidps.network.instruct.InteractInterfaceItem

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Jun 20, 2021
 */
class InterfaceOnItemOptionHandler : InstructionHandler<InteractInterfaceItem>() {

    private val itemDefinitions: ItemDefinitions by inject()
    private val interfaceDefinitions: InterfaceDefinitions by inject()
    private val containerDefinitions: ContainerDefinitions by inject()
    private val logger = InlineLogger()

    override fun validate(player: Player, instruction: InteractInterfaceItem) {
        val (fromItem, toItem, fromSlot, toSlot, fromInterfaceId, fromComponentId, toInterfaceId, toComponentId) = instruction

        val fromId = interfaceDefinitions.get(fromInterfaceId).stringId

        val fItem: Item
        val tItem: Item

        // Has interface open
        if (!player.interfaces.contains(fromId)) {
            logger.info { "Interface $fromInterfaceId not found for player $player" }
            return
        }

        // Interface has that component
        val definition = interfaceDefinitions.get(fromInterfaceId)
        val component = definition.getComponentId(fromComponentId)
        val componentDef = definition.components?.get(fromComponentId)
        if (componentDef == null) {
            logger.info { "Interface $fromInterfaceId component $fromComponentId not found for player $player" }
            return
        }

        val toId = interfaceDefinitions.get(toInterfaceId).stringId
        // Interface has that component
        val toDefinition = interfaceDefinitions.get(toInterfaceId)
        val toComponent = toDefinition.getComponentId(toComponentId)
        val toComponentDef = toDefinition.components?.get(toComponentId)
        if (toComponentDef == null) {
            logger.info { "Interface $toInterfaceId component $toComponentId not found for player $player" }
            return
        }

        // If an from item is provided
        var item = Item.EMPTY
        var fromContainer = ""
        if (fromItem != -1) {
            // Check the component name is valid
            if (component.isEmpty()) {
                logger.info { "Interface $fromId component $fromComponentId not found for player $player" }
                return
            }
            // Check the component container exists
            fromContainer = componentDef["container", ""]
            if (!player.hasContainer(fromContainer)) {
                logger.info { "Interface $fromId container $fromContainer not found for player $player" }
                return
            }

            // Check the item exists in the container
            val def = containerDefinitions.get(fromContainer)
            if (fromSlot > def.length) {
                logger.info { "Invalid interface $fromId container $fromContainer ${def.length} slot $fromSlot not found for player $player" }
                return
            }

            val itemName = itemDefinitions.get(fromItem).stringId
            val fromSlot = if (fromSlot == -1 && fromContainer == "worn_equipment") {
                player.equipment.indexOf(itemName)
            } else if (fromSlot == -1 && fromContainer == "item_loan") {
                0
            } else {
                fromSlot
            }
            val secondary = !componentDef["primary", true]
            val container = player.container(def, secondary = secondary)
            if (!container.isValidId(fromSlot, itemName)) {
                logger.info { "Interface $fromId container item $item $fromSlot not found for player $player" }
                return
            }
            item = container.getItem(fromSlot)
        }
        fItem = item

        // If an to item is provided
        item = Item.EMPTY
        var toContainer = ""
        if (toItem != -1) {
            // Check the component name is valid
            if (toComponent.isEmpty()) {
                logger.info { "Interface $toId component $toComponentId not found for player $player" }
                return
            }
            // Check the component container exists
            toContainer = toComponentDef["container", ""]
            if (!player.hasContainer(toContainer)) {
                logger.info { "Interface $toId container $toContainer not found for player $player" }
                return
            }

            // Check the item exists in the container
            val def = containerDefinitions.get(toContainer)
            if (fromSlot > def.length) {
                logger.info { "Invalid interface $fromId container $toContainer ${def.length} slot $fromSlot not found for player $player" }
                return
            }

            val itemName = itemDefinitions.get(fromItem).stringId
            val toSlot = if (toSlot == -1 && toContainer == "worn_equipment") {
                player.equipment.indexOf(itemName)
            } else if (toSlot == -1 && toContainer == "item_loan") {
                0
            } else {
                toSlot
            }
            val secondary = !componentDef["primary", true]
            val container = player.container(def, secondary = secondary)
            if (!container.isValidId(toSlot, itemName)) {
                logger.info { "Interface $toId container item $item $toSlot not found for player $player" }
                return
            }
            item = container.getItem(toSlot)
        }
        tItem = item

        sync {
            player.events.emit(
                InterfaceOnItem(
                    fromItem = fItem,
                    toItem = tItem,
                    fromSlot,
                    toSlot,
                    fromId,
                    component,
                    toId,
                    toComponent,
                    fromContainer = fromContainer,
                    toContainer = toContainer
                )
            )
        }
    }

}
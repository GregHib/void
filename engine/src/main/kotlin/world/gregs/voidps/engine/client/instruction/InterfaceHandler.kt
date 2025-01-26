package world.gregs.voidps.engine.client.instruction

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.cache.definition.data.InterfaceComponentDefinition
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.InventoryDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.equipment

/**
 * Handles operations related to game interfaces, including retrieving interface items,
 * managing component definitions, and verifying inventory items for a specified player.
 *
 * This class provides functionalities to retrieve items displayed on game interfaces,
 * validating components and their mappings to ensure proper interactions, and tracking
 * inventory items for specific interfaces.
 *
 * @property itemDefinitions Definitions and metadata of game items, providing access
 * to string identifiers and other item attributes.
 * @property interfaceDefinitions Definitions for game interfaces, including the
 * organization and metadata of components within an interface.
 * @property inventoryDefinitions Definitions for player inventories, managing inventory
 * configurations, sizes, and related behaviors.
 */
class InterfaceHandler(
    private val itemDefinitions: ItemDefinitions,
    private val interfaceDefinitions: InterfaceDefinitions,
    private val inventoryDefinitions: InventoryDefinitions
) {
    /**
     * Logger instance used for logging messages throughout the application.
     * This logger is initialized using the InlineLogger class.
     */
    private val logger = InlineLogger()

    /**
     * Retrieves information about a specific item within an interface for the given player.
     *
     * This method checks whether a player has a specific interface open, verifies the component's existence,
     * and retrieves additional details, including the associated inventory and item data if applicable.
     *
     * @param player The player for whom the interface item is being retrieved.
     * @param interfaceId The unique identifier of the interface being checked.
     * @param componentId The identifier of the specific component within the interface.
     * @param itemId The identifier of the item being queried, or -1 if not applicable.
     * @param itemSlot The slot within the inventory where the item is located, or -1 if not applicable.
     * @return An instance of [InterfaceData] containing the details of the interface and the target item,
     *         or `null` if the specified item, component, or interface conditions are not met.
     */
    fun getInterfaceItem(player: Player, interfaceId: Int, componentId: Int, itemId: Int, itemSlot: Int): InterfaceData? {
        val id = getOpenInterface(player, interfaceId) ?: return null
        val componentDefinition = getComponentDefinition(player, interfaceId, componentId) ?: return null
        val component = componentDefinition.stringId
        var item = Item.EMPTY
        var inventory = ""
        if (itemId != -1) {
            inventory = getInventory(player, id, component, componentDefinition) ?: return null
            item = getInventoryItem(player, id, componentDefinition, inventory, itemId, itemSlot) ?: return null
        }
        return InterfaceData(id, component, item, inventory, componentDefinition.options)
    }

    /**
     * Retrieves the open interface identifier for a given player and interface.
     *
     * @param player The player whose open interface is being queried.
     * @param interfaceId The ID of the interface to check.
     * @return The string identifier of the open interface if the interface is open for the player, or null if it is not open.
     */
    private fun getOpenInterface(player: Player, interfaceId: Int): String? {
        val id = interfaceDefinitions.get(interfaceId).stringId
        if (!player.interfaces.contains(id)) {
            logger.info { "Player doesn't have interface open [$player, interface=$id]" }
            return null
        }
        return id
    }

    /**
     * Retrieves the definition of a specific component within an interface.
     *
     * @param player The player requesting the component definition.
     * @param id The identifier of the interface containing the component.
     * @param componentId The identifier of the component within the interface.
     * @return The definition of the specified component, or null if the component does not exist.
     */
    private fun getComponentDefinition(player: Player, id: Int, componentId: Int): InterfaceComponentDefinition? {
        val interfaceDefinition = interfaceDefinitions.get(id)
        val componentDefinition = interfaceDefinition.components?.get(componentId)
        if (componentDefinition == null) {
            logger.info { "Interface doesn't have component [$player, interface=$id, component=$componentId]" }
            return null
        }
        return componentDefinition
    }

    /**
     * Retrieves the inventory identifier associated with a specific player and interface component.
     *
     * @param player The player for whom the inventory is being retrieved.
     * @param id The identifier of the interface this component belongs to.
     * @param component The specific component name or identifier within the interface.
     * @param componentDefinition The interface component definition containing metadata for the component.
     * @return The inventory identifier as a string if found and valid, otherwise null.
     */
    private fun getInventory(player: Player, id: String, component: String, componentDefinition: InterfaceComponentDefinition): String? {
        if (component.isEmpty()) {
            logger.info { "No inventory component found [$player, interface=$id, inventory=$component]" }
            return null
        }
        val inventory = componentDefinition["inventory", ""]
        if (!player.inventories.contains(inventory)) {
            logger.info { "Player doesn't have interface inventory [$player, interface=$id, inventory=$inventory]" }
            return null
        }
        return inventory
    }

    /**
     * Retrieves an `Item` from a player's inventory based on specified parameters.
     *
     * @param player The `Player` whose inventory is being accessed.
     * @param id The identifier for the interface or context being used.
     * @param componentDefinition The `InterfaceComponentDefinition` providing details about the interface component.
     * @param inventoryId The identifier for the inventory type (e.g., "worn_equipment", "inventory").
     * @param item The item ID to be matched, or -1 if no specific item ID is provided.
     * @param itemSlot The slot index of the item, or -1 if the slot needs to be determined dynamically.
     * @return The `Item` located at the specified slot in the inventory, or `null` if the item is not found or the parameters are invalid.
     */
    private fun getInventoryItem(player: Player, id: String, componentDefinition: InterfaceComponentDefinition, inventoryId: String, item: Int, itemSlot: Int): Item? {
        val itemId = if (item == -1 || item > itemDefinitions.size) "" else itemDefinitions.get(item).stringId
        val slot = when {
            itemSlot == -1 && inventoryId == "worn_equipment" -> player.equipment.indexOf(itemId)
            itemSlot == -1 && inventoryId == "item_loan" -> 0
            itemSlot == -1 && inventoryId == "returned_lent_items" -> 0
            id == "price_checker" -> itemSlot / 2
            inventoryId == "inventory" -> itemSlot
            else -> itemSlot
        }
        val definition = inventoryDefinitions.get(inventoryId)
        if (slot > definition.length || slot < 0) {
            logger.info { "Player interface inventory out of bounds [$player, inventory=$inventoryId, item_index=$itemSlot, inventory_size=${definition.length}]" }
            return null
        }

        val secondary = !componentDefinition["primary", true]
        val inventory = player.inventories.inventory(definition, secondary = secondary)
        if (!inventory.inBounds(slot) || inventory[slot].id != itemId) {
            logger.info { "Player invalid interface item [$player, interface=$id, index=$slot, expected_item=$itemId, actual_item=${inventory[slot]}]" }
            return null
        }
        return inventory[slot]
    }
}

/**
 * A data class representing interface data with properties for ID, component name, item
 * details, inventory name, and optional array of string options.
 *
 * @property id A unique identifier for the interface data.
 * @property component The name of the associated component.
 * @property item The item associated with the interface data.
 * @property inventory The name of the inventory related to the interface data.
 * @property options An optional array of string representations of additional options, which may be null.
 */
data class InterfaceData(
    val id: String,
    val component: String,
    val item: Item,
    val inventory: String,
    val options: Array<String?>?
) {
    /**
     * Checks if the current object is equal to another object.
     *
     * @param other The object to compare with the current instance.
     * @return `true` if the objects are equal, `false` otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InterfaceData

        if (id != other.id) return false
        if (component != other.component) return false
        if (item != other.item) return false
        if (inventory != other.inventory) return false
        if (options != null) {
            if (other.options == null) return false
            if (!options.contentEquals(other.options)) return false
        } else if (other.options != null) return false

        return true
    }

    /**
     * Computes the hash code for the object based on its properties.
     *
     * The hash code is calculated using the `id`, `component`, `item`, `inventory`,
     * and `options` properties. The calculation ensures that objects with the same
     * property values produce the same hash code, satisfying the contract of `hashCode`.
     *
     * @return The hash code value of the object.
     */
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + component.hashCode()
        result = 31 * result + item.hashCode()
        result = 31 * result + inventory.hashCode()
        result = 31 * result + (options?.contentHashCode() ?: 0)
        return result
    }
}
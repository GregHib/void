package world.gregs.voidps.engine.client.instruction

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.cache.definition.data.InterfaceComponentDefinition
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.InventoryDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.equipment

class InterfaceHandler(
    private val inventoryDefinitions: InventoryDefinitions,
    private val enumDefinitions: EnumDefinitions,
) {

    fun getInterfaceItem(player: Player, interfaceId: Int, componentId: Int, itemId: Int, itemSlot: Int): InterfaceData? {
        val id = getOpenInterface(player, interfaceId) ?: return null
        val componentDefinition = getComponentDefinition(player, interfaceId, componentId) ?: return null
        val component = componentDefinition.stringId
        var item = Item.EMPTY
        if (itemId != -1) {
            when {
                id.startsWith("summoning_") && id.endsWith("_creation") -> item = Item(ItemDefinitions.get(itemId).stringId)
                id == "summoning_trade_in" -> item = Item(ItemDefinitions.get(itemId).stringId)
                id == "exchange_item_sets" -> {
                    val expected = enumDefinitions.get("exchange_item_sets").getInt(itemSlot + 1)
                    if (expected != itemId) {
                        logger.info { "Exchange item sets don't match [$player, expected=$expected, actual=$itemId]" }
                        return null
                    }
                    item = Item(ItemDefinitions.get(expected).stringId)
                }
                id == "common_item_costs" -> item = Item(ItemDefinitions.get(itemId).stringId)
                id == "farming_equipment_store" || id == "farming_equipment_store_side" -> {}
                else -> {
                    val inventory = getInventory(player, id, component, componentDefinition) ?: return null
                    item = getInventoryItem(player, id, componentDefinition, inventory, itemId, itemSlot) ?: return null
                }
            }
        }
        return InterfaceData(id, component, item, componentDefinition.options)
    }

    private fun getOpenInterface(player: Player, interfaceId: Int): String? {
        val id = InterfaceDefinitions.get(interfaceId).stringId
        if (!player.interfaces.contains(id)) {
            logger.info { "Player doesn't have interface open [$player, interface=$id]" }
            return null
        }
        return id
    }

    private fun getComponentDefinition(player: Player, id: Int, componentId: Int): InterfaceComponentDefinition? {
        val interfaceDefinition = InterfaceDefinitions.get(id)
        val componentDefinition = interfaceDefinition.components?.get(componentId)
        if (componentDefinition == null) {
            logger.info { "Interface doesn't have component [$player, interface=$id, component=$componentId]" }
            return null
        }
        return componentDefinition
    }

    private fun getInventoryItem(player: Player, id: String, componentDefinition: InterfaceComponentDefinition, inventoryId: String, item: Int, itemSlot: Int): Item? {
        val itemId = if (item == -1 || item > ItemDefinitions.size) "" else ItemDefinitions.get(item).stringId
        val slot = when {
            itemSlot == -1 && inventoryId == "worn_equipment" -> player.equipment.indexOf(itemId)
            itemSlot == -1 && inventoryId == "item_loan" -> 0
            itemSlot == -1 && inventoryId == "returned_lent_items" -> 0
            id == "price_checker" -> itemSlot / 2
            id == "shop" -> itemSlot / 6
            id == "grand_exchange" -> componentDefinition.stringId.removePrefix("collect_slot_").toInt()
            else -> itemSlot
        }
        val definition = inventoryDefinitions.get(inventoryId)
        val secondary = !componentDefinition["primary", true]
        val inventory = player.inventories.getOrNull(definition, secondary = secondary)
        if (inventory == null) {
            logger.info { "Player invalid interface inventory [$player, interface=$id, inv=$inventoryId]" }
            return null
        }
        if (slot !in inventory.items.indices) {
            logger.info { "Player interface inventory out of bounds [$player, slot=$slot, inventory=$inventoryId, item_index=$itemSlot, inventory_size=${definition.length}, indicies=${inventory.items.indices}]" }
            return null
        }

        if (!inventory.inBounds(slot) || inventory[slot].id != itemId) {
            logger.info { "Player invalid interface item [$player, interface=$id, inv=$inventoryId, index=$slot, expected_item=$itemId, actual_item=${inventory[slot]}]" }
            return null
        }
        return inventory[slot]
    }

    companion object {
        private val logger = InlineLogger()

        fun getInventory(player: Player, id: String, component: String, componentDefinition: InterfaceComponentDefinition): String? {
            if (component.isEmpty()) {
                logger.info { "No inventory component found [$player, interface=$id, inventory=$component]" }
                return null
            }
            if (id == "shop") {
                return player["shop"]
            }
            var inventory = componentDefinition["inventory", ""]
            if (id == "grand_exchange") {
                inventory = "collection_box_${player["grand_exchange_box", -1]}"
            }
            if (inventory == "") {
                return null
            }
            if (!player.inventories.contains(inventory)) {
                logger.info { "Player doesn't have interface inventory [$player, interface=$id, inventory=$inventory]" }
                return null
            }
            return inventory
        }
    }
}

data class InterfaceData(
    val id: String,
    val component: String,
    val item: Item,
    val options: Array<String?>?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InterfaceData

        if (id != other.id) return false
        if (component != other.component) return false
        if (item != other.item) return false
        if (options != null) {
            if (other.options == null) return false
            if (!options.contentEquals(other.options)) return false
        } else if (other.options != null) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + component.hashCode()
        result = 31 * result + item.hashCode()
        result = 31 * result + (options?.contentHashCode() ?: 0)
        return result
    }
}

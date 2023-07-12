package world.gregs.voidps.engine.contain

import world.gregs.voidps.cache.config.data.InventoryDefinition
import world.gregs.voidps.engine.client.sendInventoryItems
import world.gregs.voidps.engine.contain.remove.DefaultItemRemovalChecker
import world.gregs.voidps.engine.contain.remove.ShopItemRemovalChecker
import world.gregs.voidps.engine.contain.restrict.ItemRestrictionRule
import world.gregs.voidps.engine.contain.restrict.ShopRestrictions
import world.gregs.voidps.engine.contain.stack.AlwaysStack
import world.gregs.voidps.engine.contain.stack.DependentOnItem
import world.gregs.voidps.engine.contain.stack.NeverStack
import world.gregs.voidps.engine.data.definition.InventoryDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.get

class Inventories(
    val inventories: MutableMap<String, Array<Item>> = mutableMapOf()
) : MutableMap<String, Array<Item>> by inventories {

    val instances: MutableMap<String, Inventory> = mutableMapOf()

    lateinit var definitions: InventoryDefinitions
    lateinit var itemDefinitions: ItemDefinitions
    lateinit var validItemRule: ItemRestrictionRule
    lateinit var events: Events
    lateinit var normalStack: DependentOnItem

    fun inventory(definition: InventoryDefinition, secondary: Boolean = false): Inventory {
        return inventory(definition.stringId, definition, secondary)
    }

    fun inventory(id: String, secondary: Boolean = false): Inventory {
        val definition = definitions.get(id)
        return inventory(id, definition, secondary)
    }

    fun inventory(id: String, def: InventoryDefinition, secondary: Boolean = false): Inventory {
        val shop = def["shop", false]
        val inventoryId = if (secondary) "_$id" else id
        return instances.getOrPut(inventoryId) {
            val removalCheck = if (shop) ShopItemRemovalChecker else DefaultItemRemovalChecker
            val data = inventories.getOrPut(inventoryId) {
                val ids = def.ids
                val amounts = def.amounts
                if (ids != null && amounts != null) {
                    Array(def.length) { Item(itemDefinitions.get(ids[it]).stringId, amounts[it]) }
                } else {
                    Array(def.length) { Item("", removalCheck.getMinimum(it)) }
                }
            }
            val stackRule = if (shop) AlwaysStack else when (def["stack", "normal"].lowercase()) {
                "always" -> AlwaysStack
                "never" -> NeverStack
                else -> normalStack
            }
            Inventory(
                data = data,
                id = inventoryId,
                itemRule = if (shop) ShopRestrictions(data) else validItemRule,
                stackRule = stackRule,
                removalCheck = removalCheck,
            ).apply {
                transaction.changes.bind(events)
            }
        }
    }

    fun clear(id: String, secondary: Boolean = false) {
        val inventoryId = if (secondary) "_$id" else id
        instances.remove(inventoryId)
        inventories.remove(inventoryId)
    }
}

fun Player.sendInventory(id: String, secondary: Boolean = false) {
    val definitions: InventoryDefinitions = get()
    val inventory = inventories.inventory(id, definitions.getOrNull(id) ?: return, secondary)
    sendInventory(inventory)
}

fun Player.sendInventory(inventory: Inventory, secondary: Boolean = false) {
    sendInventoryItems(
        inventory = get<InventoryDefinitions>().get(inventory.id).id,
        size = inventory.size,
        items = IntArray(inventory.size * 2) { index ->
            val item = inventory[index.rem(inventory.size)]
            if (index < inventory.size) {
                if ((inventory == this.inventory || inventory == equipment) && item.def.id == -1 && item.amount > 0) 0 else item.def.id
            } else {
                if (item.amount < 0) 0 else item.amount
            }
        },
        primary = secondary
    )
}

val Player.inventory: Inventory
    get() = inventories.inventory("inventory")

val Player.equipment: Inventory
    get() = inventories.inventory("worn_equipment")

val Player.beastOfBurden: Inventory
    get() = inventories.inventory("beast_of_burden")

fun Player.hasItem(id: String) = inventory.contains(id) || equipment.contains(id)

fun Player.hasItem(id: String, amount: Int) = inventory.contains(id, amount) || equipment.contains(id, amount)
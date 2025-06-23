package world.gregs.voidps.engine.inv

import world.gregs.voidps.cache.config.data.InventoryDefinition
import world.gregs.voidps.engine.client.sendInventoryItems
import world.gregs.voidps.engine.data.definition.InventoryDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.remove.DefaultItemAmountBounds
import world.gregs.voidps.engine.inv.remove.ShopItemAmountBounds
import world.gregs.voidps.engine.inv.restrict.ItemRestrictionRule
import world.gregs.voidps.engine.inv.restrict.ShopRestrictions
import world.gregs.voidps.engine.inv.stack.AlwaysStack
import world.gregs.voidps.engine.inv.stack.ItemStackingRule
import world.gregs.voidps.engine.inv.stack.NeverStack

class Inventories(
    private val inventories: Map<String, Array<Item>> = mutableMapOf(),
) {

    fun contains(key: String): Boolean = instances.containsKey(key)

    val instances: MutableMap<String, Inventory> = mutableMapOf()

    lateinit var definitions: InventoryDefinitions
    lateinit var itemDefinitions: ItemDefinitions
    lateinit var validItemRule: ItemRestrictionRule
    lateinit var events: EventDispatcher
    lateinit var normalStack: ItemStackingRule

    fun start() {
        for ((id, value) in inventories) {
            instances[id] = create(id, value, definitions.get(id.removePrefix("_")))
        }
        (inventories as MutableMap<*, *>).clear()
    }

    fun inventory(definition: InventoryDefinition, secondary: Boolean = false): Inventory = inventory(definition.stringId, definition, secondary)

    fun getOrNull(definition: InventoryDefinition, secondary: Boolean = false): Inventory? {
        val inventoryId = if (secondary) "_${definition.stringId}" else definition.stringId
        return instances[inventoryId]
    }

    fun inventory(id: String, secondary: Boolean = false): Inventory {
        val definition = definitions.get(id)
        return inventory(id, definition, secondary)
    }

    fun inventory(id: String, def: InventoryDefinition, secondary: Boolean = false): Inventory {
        val inventoryId = if (secondary) "_$id" else id
        return instances.getOrPut(inventoryId) {
            val ids = def.ids
            val amounts = def.amounts
            val data = if (ids != null && amounts != null) {
                Array(def.length) { Item(itemDefinitions.get(ids[it]).stringId, amounts[it]) }
            } else {
                val amountBounds = if (def["shop", false]) ShopItemAmountBounds else DefaultItemAmountBounds
                Array(def.length) { Item("", amountBounds.minimum(it)) }
            }
            create(inventoryId, data, def)
        }
    }

    private fun create(
        inventoryId: String,
        data: Array<Item>,
        def: InventoryDefinition,
    ): Inventory {
        val shop = def["shop", false]
        val amountBounds = if (shop) ShopItemAmountBounds else DefaultItemAmountBounds
        val stackRule = if (shop) {
            AlwaysStack
        } else {
            when (def["stack", "normal"].lowercase()) {
                "always" -> AlwaysStack
                "never" -> NeverStack
                else -> normalStack
            }
        }
        return Inventory(
            data = data,
            id = inventoryId,
            itemRule = if (shop) ShopRestrictions(data) else validItemRule,
            stackRule = stackRule,
            amountBounds = amountBounds,
        ).apply {
            transaction.changes.bind(events)
        }
    }

    fun clear(id: String, secondary: Boolean = false) {
        val inventoryId = if (secondary) "_$id" else id
        instances.remove(inventoryId)
    }
}

fun Player.sendInventory(id: String, secondary: Boolean = false) {
    val definitions: InventoryDefinitions = get()
    val inventory = inventories.inventory(id, definitions.getOrNull(id) ?: return, secondary)
    sendInventory(inventory)
}

fun Player.sendInventory(inventory: Inventory, secondary: Boolean = false, id: Int? = null) {
    sendInventoryItems(
        inventory = id ?: get<InventoryDefinitions>().get(inventory.id).id,
        size = inventory.size,
        items = IntArray(inventory.size * 2) { index ->
            val item = inventory[index.rem(inventory.size)]
            if (index < inventory.size) {
                if ((inventory == this.inventory || inventory == equipment) && item.def.id == -1 && item.amount > 0) 0 else item.def.id
            } else {
                if (item.amount < 0) 0 else item.amount
            }
        },
        primary = secondary,
    )
}

val Player.inventory: Inventory
    get() = inventories.inventory("inventory")

val Player.equipment: Inventory
    get() = inventories.inventory("worn_equipment")

val Player.beastOfBurden: Inventory
    get() = inventories.inventory("beast_of_burden")

fun Player.holdsItem(id: String) = inventory.contains(id) || equipment.contains(id)

fun Player.holdsItem(id: String, amount: Int) = inventory.contains(id, amount) || equipment.contains(id, amount)

package world.gregs.voidps.world.interact.entity.npc.shop

import world.gregs.voidps.engine.data.definition.InventoryDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.remove.ItemIndexRemovalChecker
import world.gregs.voidps.engine.inv.sendInventory
import world.gregs.voidps.engine.inv.stack.AlwaysStack

object GeneralStores {

    private val inventoryDefinitions: InventoryDefinitions by inject()
    private val itemDefs: ItemDefinitions by inject()

    val stores: MutableMap<String, Inventory> = mutableMapOf()

    fun get(key: String) = stores.getOrPut(key) {
        val definition = inventoryDefinitions.get(key)
        val minimumQuantities = IntArray(definition.length) {
            if (definition.getOrNull<List<Map<String, Int>>>("defaults")?.getOrNull(it) != null) -1 else 0
        }
        val checker = ItemIndexRemovalChecker(minimumQuantities, 0)
        Inventory(
            data = Array(definition.length) {
                val map = definition.getOrNull<List<Map<String, Int>>>("defaults")?.getOrNull(it)
                Item(
                    id = map?.keys?.firstOrNull() ?: "",
                    amount = map?.values?.firstOrNull() ?: 0
                )
            },
            id = key,
            itemRule = GeneralStoreRestrictions(itemDefs),
            stackRule = AlwaysStack,
            removalCheck = checker
        )
    }

    fun bind(player: Player, key: String): Inventory = get(key).apply {
        this.transaction.changes.bind(player)
        player.sendInventory(this, false)
    }

    fun unbind(player: Player, key: String): Inventory = get(key).apply {
        this.transaction.changes.unbind(player)
    }

}
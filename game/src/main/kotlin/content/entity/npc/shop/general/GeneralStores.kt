package content.entity.npc.shop.general

import world.gregs.voidps.engine.data.definition.InventoryDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.remove.ItemIndexAmountBounds
import world.gregs.voidps.engine.inv.sendInventory
import world.gregs.voidps.engine.inv.stack.AlwaysStack

object GeneralStores {

    val stores: MutableMap<String, Inventory> = mutableMapOf()

    fun get(key: String) = stores.getOrPut(key) {
        val definition = get<InventoryDefinitions>().get(key)
        val minimumQuantities = IntArray(definition.length) {
            val id = definition.ids?.getOrNull(it)
            if (id != -1 && id != null) -1 else 0
        }
        val checker = ItemIndexAmountBounds(minimumQuantities, 0)
        val itemDefinitions = get<ItemDefinitions>()
        Inventory(
            data = Array(definition.length) {
                val id = definition.ids?.getOrNull(it)
                val amount = definition.amounts?.getOrNull(it)
                if (id == null) {
                    Item.EMPTY
                } else {
                    Item(itemDefinitions.get(id).stringId, amount ?: 0)
                }
            },
            id = key,
            itemRule = GeneralStoreRestrictions(get<ItemDefinitions>()),
            stackRule = AlwaysStack,
            amountBounds = checker,
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

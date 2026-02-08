package content.bot.req.fact

import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.Inventory

class ItemView(vararg val inventories: Inventory) {
    fun contains(item: String) = inventories.any { it.contains(item) }
    fun contains(item: String, amount: Int) = inventories.any { it.contains(item, amount) }
    fun count(item: String) = inventories.sumOf { it.count(item) }
    fun count(block: (Item) -> Boolean) = inventories.sumOf { it.items.count(block) }
    fun size() = inventories.sumOf { it.size }
}

package content.entity.player.kept

import content.area.wilderness.inWilderness
import content.entity.player.effect.skulled
import content.skill.prayer.praying
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.ItemKept
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import java.util.*

object ItemsKeptOnDeath {

    fun getAllOrdered(player: Player): List<Item> = player.inventory.items
        .union(player.equipment.items.toList())
        .filter { it.isNotEmpty() }
        .sortedByDescending { it.def.cost }

    fun kept(player: Player, items: List<Item>, enums: EnumDefinitions): List<Item> {
        var save = if (player.skulled) 0 else 3
        if (player.praying("protect_item")) {
            save++
        }
        if (items.isEmpty()) {
            return emptyList()
        }
        val queue = LinkedList(items)
        val kept = mutableListOf<Item>()
        val alwaysLost = enums.get("items_lost_on_death").map!!
        var count = 0
        while (count < save) {
            val item = queue.peek() ?: break
            if (alwaysLost.getOrDefault(item.def.id, 0) == 1) {
                queue.pop()
                continue
            }
            when (val type = item.def["kept", ItemKept.Never]) {
                ItemKept.Never, ItemKept.Vanish -> {
                    queue.pop()
                    continue
                }
                ItemKept.Always, ItemKept.Reclaim, ItemKept.Wilderness -> {
                    if (type == ItemKept.Wilderness && player.inWilderness) {
                        queue.pop()
                        continue
                    }
                    if (item.amount == 1) {
                        kept.add(queue.pop())
                    } else if (item.amount > 1) {
                        queue.pop()
                        queue.addFirst(item.copy(amount = item.amount - 1))
                        kept.add(item.copy(amount = 1))
                    }
                }
            }
            count++
        }
        return kept
    }
}

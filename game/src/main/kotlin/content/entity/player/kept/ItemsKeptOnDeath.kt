package content.entity.player.kept

import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.world.interact.entity.player.combat.prayer.praying
import world.gregs.voidps.world.interact.entity.player.effect.skulled
import java.util.*

object ItemsKeptOnDeath {

    fun getAllOrdered(player: Player): List<Item> {
        return player.inventory.items
            .union(player.equipment.items.toList())
            .filter { it.isNotEmpty() }
            .sortedByDescending { it.def.cost }
    }

    fun kept(player: Player, items: List<Item>, enums: EnumDefinitions = get()): List<Item> {
        var save = if (player.skulled) 0 else 3
        if (player.praying("protect_item")) {
            save++
        }
        if (items.isEmpty()) {
            return emptyList()
        }
        val queue = LinkedList(items)
        val kept = mutableListOf<Item>()
        val alwaysLost = enums.get(616).map!!
        var count = 0
        while (count < save) {
            val item = queue.peek() ?: break
            if (alwaysLost.getOrDefault(item.def.id, 0) == 1) {
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
            count++
        }
        return kept
    }
}
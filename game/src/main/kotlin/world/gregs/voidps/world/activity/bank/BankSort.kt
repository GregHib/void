package world.gregs.voidps.world.activity.bank

import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.item.Item
import java.util.*

fun Container.sort() {
    val all = LinkedList<Item>()
    for (index in indices.reversed()) {
        val item = get(index)
        if (shouldRemove(item.amount, index)) {
            all.addLast(item)
        } else {
            all.addFirst(item)
        }
    }
    transaction {
        clear()
        all.forEachIndexed { index, item ->
            set(index, item)
        }
    }
}
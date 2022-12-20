package world.gregs.voidps.world.activity.bank

import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.item.Item
import java.util.*

fun Container.sort() {
    val all = LinkedList<Item>()
    for ((index, item) in this.items.withIndex().reversed()) {
        if (removalCheck.shouldRemove(index, item.amount)) {
            all.addLast(item)
        } else {
            all.addFirst(item)
        }
    }
    all.forEachIndexed { index, item ->
        this.items[index] = item
    }
}
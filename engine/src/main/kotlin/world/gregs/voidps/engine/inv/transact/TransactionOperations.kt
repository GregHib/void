package world.gregs.voidps.engine.inv.transact

import world.gregs.voidps.engine.entity.item.Item

fun Transaction.remove(items: List<Item>) {
    for (item in items) {
        remove(item.id, item.amount)
    }
}

@JvmName("removePairs")
fun Transaction.remove(items: List<Pair<String, Int>>) {
    for ((id, amount) in items) {
        remove(id, amount)
    }
}
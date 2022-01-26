package world.gregs.voidps.network.encode

import io.ktor.utils.io.*
import world.gregs.voidps.network.*
import world.gregs.voidps.network.Client.Companion.SHORT
import world.gregs.voidps.network.Protocol.INTERFACE_ITEMS

/**
 * Sends a list of items to display on an interface item group component
 * @param container The id of the container
 * @param items List of the item ids to display
 * @param amounts List of the item amounts to display
 * @param primary Optional to send to the primary or secondary container
 */
fun Client.sendContainerItems(
    container: Int,
    items: IntArray,
    amounts: IntArray,
    primary: Boolean
) {
    send(INTERFACE_ITEMS, getLength(items, amounts), SHORT) {
        writeShort(container)
        writeByte(primary)
        writeShort(items.size)
        for ((index, item) in items.withIndex()) {
            val amount = amounts[index]
            writeByteAdd(if (amount >= 255) 255 else amount)
            if (amount >= 255) {
                writeIntMiddle(amount)
            }
            writeShortAdd(item + 1)
        }
    }
}

private fun getLength(items: IntArray, amounts: IntArray): Int {
    var count = 5
    count += amounts.sumOf(::large)
    count += items.size * 2
    return count
}

private fun large(it: Int): Int {
    return if (it >= 255) 5 else 1
}
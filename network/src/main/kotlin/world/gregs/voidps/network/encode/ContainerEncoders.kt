package world.gregs.voidps.network.encode

import io.ktor.utils.io.*
import world.gregs.voidps.network.Protocol.INTERFACE_ITEMS
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.client.Client.Companion.SHORT
import world.gregs.voidps.network.writeByte
import world.gregs.voidps.network.writeByteAdd
import world.gregs.voidps.network.writeIntMiddle
import world.gregs.voidps.network.writeShortAdd

/**
 * Sends a list of items to display on an interface item group component
 * @param inventory The id of the inventory
 * @param size The capacity of items in the inventory
 * @param items List of the item ids and amounts to display
 * @param primary Optional to send to the primary or secondary inventory
 */
fun Client.sendInventoryItems(
    inventory: Int,
    size: Int,
    items: IntArray,
    primary: Boolean
) {
    send(INTERFACE_ITEMS, getLength(items, size), SHORT) {
        writeShort(inventory)
        writeByte(primary)
        writeShort(size)
        for (index in 0 until size) {
            val item = items[index]
            val amount = items[size + index]
            writeByteAdd(if (amount >= 255) 255 else amount)
            if (amount >= 255) {
                writeIntMiddle(amount)
            }
            writeShortAdd(item + 1)
        }
    }
}

private fun getLength(items: IntArray, size: Int): Int {
    var count = 5
    for (i in 0 until size) {
        count += large(items[size + i])
    }
    count += items.size
    return count
}

private fun large(it: Int): Int {
    return if (it >= 255) 5 else 1
}
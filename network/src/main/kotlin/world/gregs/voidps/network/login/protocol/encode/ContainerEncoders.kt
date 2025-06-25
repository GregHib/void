package world.gregs.voidps.network.login.protocol.encode

import io.ktor.utils.io.*
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.client.Client.Companion.SHORT
import world.gregs.voidps.network.login.Protocol.UPDATE_INV_FULL
import world.gregs.voidps.network.login.protocol.*

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
    send(UPDATE_INV_FULL, getLength(items, size), SHORT) {
        writeShort(inventory)
        writeByte(primary)
        writeShort(size)
        for (index in 0 until size) {
            val item = items[index]
            val amount = items[size + index]
            writeByte(if (amount >= 255) 255 else amount)
            if (amount >= 255) {
                writeInt(amount)
            }
            ip2(item + 1)
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
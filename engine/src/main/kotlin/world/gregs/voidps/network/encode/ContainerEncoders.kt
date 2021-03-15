package world.gregs.voidps.network.encode

import io.ktor.utils.io.*
import world.gregs.voidps.buffer.write.writeByte
import world.gregs.voidps.buffer.write.writeByteAdd
import world.gregs.voidps.buffer.write.writeIntMiddle
import world.gregs.voidps.buffer.write.writeShortAdd
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.GameOpcodes.INTERFACE_ITEMS
import world.gregs.voidps.network.PacketSize.SHORT

/**
 * Sends a list of items to display on a interface item group component
 * @param container The id of the container
 * @param items List of the item ids to display
 * @param amounts List of the item amounts to display
 * @param primary Optional to send to the primary or secondary container
 */
fun Player.sendContainerItems(
    container: Int,
    items: IntArray,
    amounts: IntArray,
    primary: Boolean
) {
    client?.send(INTERFACE_ITEMS, getLength(items, amounts), SHORT) {
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
    count += amounts.sumBy { if (it >= 255) 5 else 1 }
    count += items.size * 2
    return count
}
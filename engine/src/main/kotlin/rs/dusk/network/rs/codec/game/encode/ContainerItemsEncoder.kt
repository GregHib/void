package rs.dusk.network.rs.codec.game.encode

import rs.dusk.buffer.Endian
import rs.dusk.buffer.write.writeByte
import rs.dusk.buffer.write.writeShort
import rs.dusk.core.network.codec.message.Encoder
import rs.dusk.core.network.codec.packet.PacketSize
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.rs.codec.game.GameOpcodes.INTERFACE_ITEMS
import rs.dusk.utility.get

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since July 31, 2020
 */
class ContainerItemsEncoder : Encoder(INTERFACE_ITEMS, PacketSize.SHORT) {

    /**
     * Sends a list of items to display on a interface item group component
     * @param key The id of the container
     * @param items List of the item ids to display
     * @param amounts List of the item amounts to display
     * @param primary Optional to send to the primary or secondary container
     */
    fun encode(
        player: Player,
        key: Int,
        items: IntArray,
        amounts: IntArray,
        primary: Boolean
    ) = player.send(getLength(items, amounts)) {
        writeShort(key)
        writeByte(primary)
        writeShort(items.size)
        for ((index, item) in items.withIndex()) {
            val amount = amounts[index]
            writeByte(if (amount >= 255) 255 else amount)
            if (amount >= 255) {
                writeInt(amount)
            }
            writeShort(item + 1, order = Endian.LITTLE)
        }
    }

    private fun getLength(items: IntArray, amounts: IntArray): Int {
        var count = 5
        count += amounts.sumBy { if (it >= 255) 5 else 1 }
        count += items.size * 2
        return count
    }
}

fun Player.sendContainerItems(
    container: Int,
    items: IntArray,
    amounts: IntArray,
    primary: Boolean
) = get<ContainerItemsEncoder>()
    .encode(this,
        container,
        items,
        amounts,
        primary
    )
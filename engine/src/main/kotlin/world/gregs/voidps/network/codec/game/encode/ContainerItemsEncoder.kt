package world.gregs.voidps.network.codec.game.encode

import world.gregs.voidps.buffer.Endian
import world.gregs.voidps.buffer.Modifier
import world.gregs.voidps.buffer.write.writeByte
import world.gregs.voidps.buffer.write.writeInt
import world.gregs.voidps.buffer.write.writeShort
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.codec.Encoder
import world.gregs.voidps.network.codec.game.GameOpcodes.INTERFACE_ITEMS
import world.gregs.voidps.network.packet.PacketSize
import world.gregs.voidps.utility.get

/**
 * @author GregHib <greg@gregs.world>
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
            writeByte(if (amount >= 255) 255 else amount, Modifier.ADD)
            if (amount >= 255) {
                writeInt(amount, order = Endian.MIDDLE)
            }
            writeShort(item + 1, Modifier.ADD)
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
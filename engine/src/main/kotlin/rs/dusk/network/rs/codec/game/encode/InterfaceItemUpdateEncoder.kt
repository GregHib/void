package rs.dusk.network.rs.codec.game.encode

import rs.dusk.buffer.write.writeByte
import rs.dusk.buffer.write.writeSmart
import rs.dusk.core.network.codec.message.Encoder
import rs.dusk.core.network.codec.packet.PacketSize
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.rs.codec.game.GameOpcodes.INTERFACE_ITEMS_UPDATE
import rs.dusk.utility.get

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since July 31, 2020
 */
class InterfaceItemUpdateEncoder : Encoder(INTERFACE_ITEMS_UPDATE, PacketSize.SHORT) {

    /**
     * Sends a list of items to display on a interface item group component
     * @param key The id of the interface item group
     * @param updates List of the indices, item ids and amounts to update
     * @param primary Optional to send to the primary or secondary container
     */
    fun encode(
        player: Player,
        key: Int,
        updates: List<Triple<Int, Int, Int>>,
        primary: Boolean
    ) = player.send(getLength(updates)) {
        writeShort(key)
        writeByte(primary)
        for ((index, item, amount) in updates) {
            writeSmart(index)
            writeShort(item + 1)
            if (item >= 0) {
                writeByte(if (amount >= 255) 255 else amount)
                if (amount >= 255) {
                    writeInt(amount)
                }
            }
        }
    }

    private fun getLength(updates: List<Triple<Int, Int, Int>>): Int {
        return 3 + updates.sumBy { (index, item, amount) -> smart(index) + if (item >= 0) if (amount >= 255) 7 else 3 else 2 }
    }
}

fun Player.sendInterfaceItemUpdate(
    key: Int,
    updates: List<Triple<Int, Int, Int>>,
    primary: Boolean
) = get<InterfaceItemUpdateEncoder>()
    .encode(this,
        key,
        updates,
        primary
    )
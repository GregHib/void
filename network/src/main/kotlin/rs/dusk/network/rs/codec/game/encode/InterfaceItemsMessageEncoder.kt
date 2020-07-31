package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.io.Endian
import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.core.network.model.packet.PacketType
import rs.dusk.network.rs.codec.game.GameMessageEncoder
import rs.dusk.network.rs.codec.game.GameOpcodes.INTERFACE_ITEMS
import rs.dusk.network.rs.codec.game.encode.message.InterfaceItemsMessage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since July 31, 2020
 */
class InterfaceItemsMessageEncoder : GameMessageEncoder<InterfaceItemsMessage>() {

    override fun encode(builder: PacketWriter, msg: InterfaceItemsMessage) {
        val (key, items, amounts, negativeKey) = msg
        builder.apply {
            writeOpcode(INTERFACE_ITEMS, PacketType.SHORT)
            writeShort(if (negativeKey) key else key)
            writeByte(negativeKey)
            writeShort(items.size)
            for((index, item) in items.withIndex()) {
                val amount = amounts[index]
                writeByte(if (amount >= 255) 255 else amount)
                if (amount >= 255) {
                    writeInt(amount)
                }
                writeShort(item + 1, order = Endian.LITTLE)
            }
        }
    }
}
package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.core.network.model.packet.PacketType
import rs.dusk.network.rs.codec.game.GameMessageEncoder
import rs.dusk.network.rs.codec.game.GameOpcodes.INTERFACE_ITEMS_UPDATE
import rs.dusk.network.rs.codec.game.encode.message.InterfaceItemUpdateMessage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since July 31, 2020
 */
class InterfaceItemUpdateMessageEncoder : GameMessageEncoder<InterfaceItemUpdateMessage>() {

    override fun encode(builder: PacketWriter, msg: InterfaceItemUpdateMessage) {
        val (key, updates, negativeKey) = msg
        builder.apply {
            writeOpcode(INTERFACE_ITEMS_UPDATE, PacketType.SHORT)
            writeShort(key)
            writeByte(negativeKey)
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
    }
}
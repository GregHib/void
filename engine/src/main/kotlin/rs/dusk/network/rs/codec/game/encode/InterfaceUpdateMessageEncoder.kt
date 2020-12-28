package rs.dusk.network.rs.codec.game.encode

import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.core.network.codec.message.MessageEncoder
import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.core.network.model.packet.PacketType
import rs.dusk.network.rs.codec.game.GameOpcodes.INTERFACE_WINDOW
import rs.dusk.network.rs.codec.game.encode.message.InterfaceUpdateMessage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 18, 2020
 */
class InterfaceUpdateMessageEncoder : MessageEncoder<InterfaceUpdateMessage> {

    override fun encode(builder: PacketWriter, msg: InterfaceUpdateMessage) {
        val (id, type) = msg
        builder.apply {
            writeOpcode(INTERFACE_WINDOW, PacketType.FIXED)
            writeShort(id, Modifier.ADD, Endian.LITTLE)
            writeByte(type, Modifier.SUBTRACT)
        }
    }
}
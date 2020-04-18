package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.io.Endian
import rs.dusk.core.io.Modifier
import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.core.network.model.packet.PacketType
import rs.dusk.network.rs.codec.game.GameMessageEncoder
import rs.dusk.network.rs.codec.game.GameOpcodes.INTERFACE_WINDOW
import rs.dusk.network.rs.codec.game.encode.message.WindowUpdateMessage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 18, 2020
 */
class WindowUpdateMessageEncoder : GameMessageEncoder<WindowUpdateMessage>() {

    override fun encode(builder: PacketWriter, msg: WindowUpdateMessage) {
        val (id, type) = msg
        builder.apply {
            writeOpcode(INTERFACE_WINDOW, PacketType.FIXED)
            writeShort(id, Modifier.ADD, Endian.LITTLE)
            writeByte(type, Modifier.SUBTRACT)
        }
    }
}
package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.io.Endian
import rs.dusk.core.io.Modifier
import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.network.rs.codec.game.GameMessageEncoder
import rs.dusk.network.rs.codec.game.GameOpcodes.INTERFACE_OPEN
import rs.dusk.network.rs.codec.game.encode.message.InterfaceOpenMessage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since July 25, 2020
 */
class InterfaceOpenMessageEncoder : GameMessageEncoder<InterfaceOpenMessage>() {

    override fun encode(builder: PacketWriter, msg: InterfaceOpenMessage) {
        val (permanent, parent, component, id) = msg
        builder.apply {
            writeOpcode(INTERFACE_OPEN)
            writeShort(id, Modifier.ADD, Endian.LITTLE)
            writeInt(parent shl 16 or component, order = Endian.LITTLE)
            writeByte(permanent)
        }
    }
}
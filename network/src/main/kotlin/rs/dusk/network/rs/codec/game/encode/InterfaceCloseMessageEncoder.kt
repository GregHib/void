package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.io.Endian
import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.network.rs.codec.game.GameMessageEncoder
import rs.dusk.network.rs.codec.game.GameOpcodes.INTERFACE_CLOSE
import rs.dusk.network.rs.codec.game.encode.message.InterfaceCloseMessage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since July 25, 2020
 */
class InterfaceCloseMessageEncoder : GameMessageEncoder<InterfaceCloseMessage>() {

    override fun encode(builder: PacketWriter, msg: InterfaceCloseMessage) {
        val (id, component) = msg
        builder.apply {
            writeOpcode(INTERFACE_CLOSE)
            writeInt(id shl 16 or component, order = Endian.LITTLE)
        }
    }
}
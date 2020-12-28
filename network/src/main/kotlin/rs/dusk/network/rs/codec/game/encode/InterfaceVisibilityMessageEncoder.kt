package rs.dusk.network.rs.codec.game.encode

import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.core.network.codec.message.MessageEncoder
import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.network.rs.codec.game.GameOpcodes.INTERFACE_COMPONENT_VISIBILITY
import rs.dusk.network.rs.codec.game.encode.message.InterfaceVisibilityMessage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since August 2, 2020
 */
class InterfaceVisibilityMessageEncoder : MessageEncoder<InterfaceVisibilityMessage>() {

    override fun encode(builder: PacketWriter, msg: InterfaceVisibilityMessage) {
        val (id, component, visible) = msg
        builder.apply {
            writeOpcode(INTERFACE_COMPONENT_VISIBILITY)
            writeInt(id shl 16 or component, order = Endian.MIDDLE)
            writeByte(visible, Modifier.ADD)
        }
    }
}
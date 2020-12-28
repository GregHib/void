package rs.dusk.network.rs.codec.game.encode

import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.core.network.codec.message.MessageEncoder
import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.network.rs.codec.game.GameOpcodes.INTERFACE_ANIMATION
import rs.dusk.network.rs.codec.game.encode.message.InterfaceAnimationMessage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since August 02, 2020
 */
class InterfaceMessageEncoder : MessageEncoder<InterfaceAnimationMessage>() {

    override fun encode(builder: PacketWriter, msg: InterfaceAnimationMessage) {
        val (id, component, animation) = msg
        builder.apply {
            writeOpcode(INTERFACE_ANIMATION)
            writeShort(animation, Modifier.ADD, Endian.LITTLE)
            writeInt(id shl 16 or component, order = Endian.MIDDLE)
        }
    }
}
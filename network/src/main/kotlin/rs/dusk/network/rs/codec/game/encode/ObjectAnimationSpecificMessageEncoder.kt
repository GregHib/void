package rs.dusk.network.rs.codec.game.encode

import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.core.network.codec.message.MessageEncoder
import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.network.rs.codec.game.GameOpcodes.OBJECT_ANIMATION_SPECIFIC
import rs.dusk.network.rs.codec.game.encode.message.ObjectAnimationSpecificMessage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since June 27, 2020
 */
class ObjectAnimationSpecificMessageEncoder : MessageEncoder<ObjectAnimationSpecificMessage>() {

    override fun encode(builder: PacketWriter, msg: ObjectAnimationSpecificMessage) {
        val (tile, animation, type, rotation) = msg
        builder.apply {
            writeOpcode(OBJECT_ANIMATION_SPECIFIC)
            writeShort(animation, type = Modifier.ADD, order = Endian.LITTLE)
            writeByte(tile, type = Modifier.ADD)
            writeByte((type shl 2) or rotation)
        }
    }
}
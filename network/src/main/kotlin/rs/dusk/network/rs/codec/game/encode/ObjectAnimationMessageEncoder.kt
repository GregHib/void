package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.io.Endian
import rs.dusk.core.io.Modifier
import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.network.rs.codec.game.GameMessageEncoder
import rs.dusk.network.rs.codec.game.GameOpcodes.OBJECT_ANIMATION
import rs.dusk.network.rs.codec.game.encode.message.ObjectAnimationMessage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since June 27, 2020
 */
class ObjectAnimationMessageEncoder : GameMessageEncoder<ObjectAnimationMessage>() {

    override fun encode(builder: PacketWriter, msg: ObjectAnimationMessage) {
        val (tile, animation, type, rotation) = msg
        builder.apply {
            writeOpcode(OBJECT_ANIMATION)
            writeInt(tile, order= Endian.MIDDLE, type = Modifier.INVERSE)
            writeShort(animation, type = Modifier.ADD)
            writeByte((type shl 2) or rotation)
        }
    }
}
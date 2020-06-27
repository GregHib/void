package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.io.Modifier
import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.network.rs.codec.game.GameMessageEncoder
import rs.dusk.network.rs.codec.game.GameOpcodes.OBJECT_REMOVE
import rs.dusk.network.rs.codec.game.encode.message.ObjectRemoveMessage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since June 27, 2020
 */
class ObjectRemoveMessageEncoder : GameMessageEncoder<ObjectRemoveMessage>() {

    override fun encode(builder: PacketWriter, msg: ObjectRemoveMessage) {
        val (tile, type, rotation) = msg
        builder.apply {
            writeOpcode(OBJECT_REMOVE)
            writeByte(tile, type = Modifier.INVERSE)
            writeByte((type shl 2) or rotation)
        }
    }
}
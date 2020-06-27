package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.io.Modifier
import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.network.rs.codec.game.GameMessageEncoder
import rs.dusk.network.rs.codec.game.GameOpcodes.OBJECT_ADD
import rs.dusk.network.rs.codec.game.encode.message.ObjectAddMessage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since June 27, 2020
 */
class ObjectAddMessageEncoder : GameMessageEncoder<ObjectAddMessage>() {

    override fun encode(builder: PacketWriter, msg: ObjectAddMessage) {
        val (tile, id, type, rotation) = msg
        builder.apply {
            writeOpcode(OBJECT_ADD)
            writeByte(tile)
            writeByte((type shl 2) or rotation)
            writeShort(id, type = Modifier.ADD)
        }
    }
}
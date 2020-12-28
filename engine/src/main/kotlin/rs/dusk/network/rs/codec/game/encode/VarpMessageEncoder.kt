package rs.dusk.network.rs.codec.game.encode

import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.core.network.codec.message.MessageEncoder
import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.network.rs.codec.game.GameOpcodes.CLIENT_VARP
import rs.dusk.network.rs.codec.game.GameOpcodes.CLIENT_VARP_LARGE
import rs.dusk.network.rs.codec.game.encode.message.VarpMessage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since July 04, 2020
 */
class VarpMessageEncoder : MessageEncoder<VarpMessage> {

    override fun encode(builder: PacketWriter, msg: VarpMessage) {
        val (id, value, large) = msg
        builder.apply {
            writeOpcode(if(large) CLIENT_VARP_LARGE else CLIENT_VARP)
            if(large) {
                writeInt(value, Modifier.INVERSE, Endian.MIDDLE)
                writeShort(id, Modifier.ADD)
            } else {
                writeShort(id)
                writeByte(value, Modifier.ADD)
            }
        }
    }
}
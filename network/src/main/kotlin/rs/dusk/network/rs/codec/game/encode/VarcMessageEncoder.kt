package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.io.Endian
import rs.dusk.core.io.Modifier
import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.network.rs.codec.game.GameMessageEncoder
import rs.dusk.network.rs.codec.game.GameOpcodes.CLIENT_VARC
import rs.dusk.network.rs.codec.game.GameOpcodes.CLIENT_VARC_LARGE
import rs.dusk.network.rs.codec.game.encode.message.VarcMessage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since July 04, 2020
 */
class VarcMessageEncoder : GameMessageEncoder<VarcMessage>() {

    override fun encode(builder: PacketWriter, msg: VarcMessage) {
        val (id, value, large) = msg
        builder.apply {
            writeOpcode(if(large) CLIENT_VARC_LARGE else CLIENT_VARC)
            if(large) {
                writeShort(id, order = Endian.LITTLE)
                writeInt(value)
            } else {
                writeShort(id, Modifier.ADD, Endian.LITTLE)
                writeByte(value, Modifier.SUBTRACT)
            }
        }
    }
}
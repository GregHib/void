package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.io.Modifier
import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.network.rs.codec.game.GameMessageEncoder
import rs.dusk.network.rs.codec.game.GameOpcodes.CLIENT_VARBIT
import rs.dusk.network.rs.codec.game.GameOpcodes.CLIENT_VARBIT_LARGE
import rs.dusk.network.rs.codec.game.encode.message.VarbitMessage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since July 04, 2020
 */
class VarbitMessageEncoder : GameMessageEncoder<VarbitMessage>() {

    override fun encode(builder: PacketWriter, msg: VarbitMessage) {
        val (id, value, large) = msg
        builder.apply {
            writeOpcode(if(large) CLIENT_VARBIT_LARGE else CLIENT_VARBIT)
            if(large) {
                writeInt(value)
                writeShort(id)
            } else {
                writeByte(value, type = Modifier.SUBTRACT)
                writeShort(id, type = Modifier.ADD)
            }
        }
    }
}
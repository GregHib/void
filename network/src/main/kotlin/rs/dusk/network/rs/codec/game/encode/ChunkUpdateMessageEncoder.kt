package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.io.Modifier
import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.network.rs.codec.game.GameMessageEncoder
import rs.dusk.network.rs.codec.game.GameOpcodes.UPDATE_CHUNK
import rs.dusk.network.rs.codec.game.encode.message.ChunkUpdateMessage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since June 19, 2020
 */
class ChunkUpdateMessageEncoder : GameMessageEncoder<ChunkUpdateMessage>() {

    override fun encode(builder: PacketWriter, msg: ChunkUpdateMessage) {
        val (x, y, plane) = msg
        builder.apply {
            writeOpcode(UPDATE_CHUNK)
            writeByte(x, Modifier.ADD)
            writeByte(y)
            writeByte(plane, type = Modifier.SUBTRACT)
        }
    }
}
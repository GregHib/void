package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.io.Modifier
import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.network.rs.codec.game.GameMessageEncoder
import rs.dusk.network.rs.codec.game.GameOpcodes.CHUNK_CLEAR
import rs.dusk.network.rs.codec.game.encode.message.ChunkClearMessage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since June 21, 2020
 */
class ChunkClearMessageEncoder : GameMessageEncoder<ChunkClearMessage>() {

    override fun encode(builder: PacketWriter, msg: ChunkClearMessage) {
        val (x, y, plane) = msg
        builder.apply {
            writeOpcode(CHUNK_CLEAR)
            writeByte(x)
            writeByte(y, Modifier.SUBTRACT)
            writeByte(plane)
        }
    }
}
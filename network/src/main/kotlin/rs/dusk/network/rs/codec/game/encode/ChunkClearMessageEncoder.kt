package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.io.Modifier
import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.network.rs.codec.game.GameMessageEncoder
import rs.dusk.network.rs.codec.game.GameOpcodes.FLOOR_ITEM_CLEAR
import rs.dusk.network.rs.codec.game.encode.message.ChunkClearMessage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since June 21, 2020
 */
class ChunkClearMessageEncoder : GameMessageEncoder<ChunkClearMessage>() {

    override fun encode(builder: PacketWriter, msg: ChunkClearMessage) {
        val (x, y, plane) = msg
        builder.apply {
            writeOpcode(FLOOR_ITEM_CLEAR)// TODO is this just floor item clear or chunk clear?
            writeByte(x)
            writeByte(y, Modifier.SUBTRACT)
            writeByte(plane)
        }
    }
}
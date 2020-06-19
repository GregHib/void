package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.io.Modifier
import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.network.rs.codec.game.GameMessageEncoder
import rs.dusk.network.rs.codec.game.GameOpcodes.FLOOR_ITEM_ADD
import rs.dusk.network.rs.codec.game.encode.message.FloorItemAddMessage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since June 19, 2020
 */
class FloorItemAddMessageEncoder : GameMessageEncoder<FloorItemAddMessage>() {

    override fun encode(builder: PacketWriter, msg: FloorItemAddMessage) {
        val (tile, id, amount) = msg
        builder.apply {
            writeOpcode(FLOOR_ITEM_ADD)
            writeByte(tile, type = Modifier.INVERSE)
            writeShort(id, type = Modifier.ADD)
            writeShort(amount)
        }
    }
}
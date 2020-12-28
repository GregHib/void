package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.network.codec.message.MessageEncoder
import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.network.rs.codec.game.GameOpcodes.FLOOR_ITEM_REMOVE
import rs.dusk.network.rs.codec.game.encode.message.FloorItemRemoveMessage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since June 19, 2020
 */
class FloorItemRemoveMessageEncoder : MessageEncoder<FloorItemRemoveMessage> {

    override fun encode(builder: PacketWriter, msg: FloorItemRemoveMessage) {
        val (tile, id) = msg
        builder.apply {
            writeOpcode(FLOOR_ITEM_REMOVE)
            writeShort(id)
            writeByte(tile)
        }
    }
}
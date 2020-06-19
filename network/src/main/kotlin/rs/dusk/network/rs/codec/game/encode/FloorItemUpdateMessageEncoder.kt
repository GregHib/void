package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.network.rs.codec.game.GameMessageEncoder
import rs.dusk.network.rs.codec.game.GameOpcodes.FLOOR_ITEM_UPDATE
import rs.dusk.network.rs.codec.game.encode.message.FloorItemUpdateMessage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since June 19, 2020
 */
class FloorItemUpdateMessageEncoder : GameMessageEncoder<FloorItemUpdateMessage>() {

    override fun encode(builder: PacketWriter, msg: FloorItemUpdateMessage) {
        val (tile, id, oldAmount, newAmount) = msg
        builder.apply {
            writeOpcode(FLOOR_ITEM_UPDATE)
            writeByte(tile)
            writeShort(id)
            writeShort(oldAmount)
            writeShort(newAmount)
        }
    }
}
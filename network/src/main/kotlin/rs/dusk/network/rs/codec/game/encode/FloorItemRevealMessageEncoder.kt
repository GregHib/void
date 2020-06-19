package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.io.Endian
import rs.dusk.core.io.Modifier
import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.network.rs.codec.game.GameMessageEncoder
import rs.dusk.network.rs.codec.game.GameOpcodes.FLOOR_ITEM_REVEAL
import rs.dusk.network.rs.codec.game.encode.message.FloorItemRevealMessage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since June 19, 2020
 */
class FloorItemRevealMessageEncoder : GameMessageEncoder<FloorItemRevealMessage>() {

    override fun encode(builder: PacketWriter, msg: FloorItemRevealMessage) {
        val (tile, id, amount, owner) = msg
        builder.apply {
            writeOpcode(FLOOR_ITEM_REVEAL)
            writeShort(owner, type = Modifier.ADD)
            writeByte(tile, type = Modifier.ADD)
            writeShort(id, order = Endian.LITTLE)
            writeShort(amount)
        }
    }
}
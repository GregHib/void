package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.io.Endian
import rs.dusk.core.io.Modifier
import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.core.network.model.packet.PacketType
import rs.dusk.network.rs.codec.game.GameMessageEncoder
import rs.dusk.network.rs.codec.game.GameOpcodes.PLAYER_OPTION
import rs.dusk.network.rs.codec.game.encode.message.ContextMenuOptionMessage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since August 16, 2020
 */
class ContextMenuOptionMessageEncoder : GameMessageEncoder<ContextMenuOptionMessage>() {

    override fun encode(builder: PacketWriter, msg: ContextMenuOptionMessage) {
        val (option, slot, top, cursor) = msg
        builder.apply {
            writeOpcode(PLAYER_OPTION, PacketType.BYTE)
            writeByte(top, Modifier.ADD)
            writeShort(cursor, order = Endian.LITTLE)
            writeString(option)
            writeByte(slot, Modifier.INVERSE)
        }
    }
}
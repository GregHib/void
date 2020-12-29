package rs.dusk.network.rs.codec.game.encode

import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.core.network.codec.message.MessageEncoder
import rs.dusk.core.network.codec.packet.access.PacketSize
import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.network.rs.codec.game.GameOpcodes.PLAYER_OPTION
import rs.dusk.network.rs.codec.game.encode.message.ContextMenuOptionMessage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since August 16, 2020
 */
class ContextMenuOptionMessageEncoder : MessageEncoder<ContextMenuOptionMessage> {

    override fun encode(builder: PacketWriter, msg: ContextMenuOptionMessage) {
        val (option, slot, top, cursor) = msg
        builder.apply {
            writeOpcode(PLAYER_OPTION, PacketSize.BYTE)
            writeByte(top, Modifier.ADD)
            writeShort(cursor, order = Endian.LITTLE)
            writeString(option)
            writeByte(slot, Modifier.INVERSE)
        }
    }
}
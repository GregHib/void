package rs.dusk.network.rs.codec.game.encode

import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.core.network.codec.message.MessageEncoder
import rs.dusk.core.network.codec.packet.access.PacketSize
import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.network.rs.codec.game.GameOpcodes.CLIENT_VARC_STR
import rs.dusk.network.rs.codec.game.encode.message.VarcStrMessage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since July 04, 2020
 */
class VarcStrMessageEncoder : MessageEncoder<VarcStrMessage> {

    override fun encode(builder: PacketWriter, msg: VarcStrMessage) {
        val (id, value) = msg
        builder.apply {
            writeOpcode(CLIENT_VARC_STR, PacketSize.BYTE)
            writeShort(id, Modifier.ADD, Endian.LITTLE)
            writeString(value)
        }
    }
}
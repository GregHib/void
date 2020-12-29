package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.network.codec.message.MessageEncoder
import rs.dusk.core.network.codec.packet.access.PacketSize
import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.network.rs.codec.game.GameOpcodes.TILE_TEXT
import rs.dusk.network.rs.codec.game.encode.message.TextTileMessage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since June 27, 2020
 */
class TextTileMessageEncoder : MessageEncoder<TextTileMessage> {

    override fun encode(builder: PacketWriter, msg: TextTileMessage) {
        val (tile, duration, height, color, text) = msg
        builder.apply {
            writeOpcode(TILE_TEXT, PacketSize.BYTE)
            writeByte(0)
            writeByte(tile)
            writeShort(duration)
            writeByte(height)
            writeMedium(color)
            writeString(text)
        }
    }
}
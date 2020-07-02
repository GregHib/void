package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.core.network.model.packet.PacketType
import rs.dusk.network.rs.codec.game.GameMessageEncoder
import rs.dusk.network.rs.codec.game.GameOpcodes.TILE_TEXT
import rs.dusk.network.rs.codec.game.encode.message.TextTileMessage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since June 27, 2020
 */
class TextTileMessageEncoder : GameMessageEncoder<TextTileMessage>() {

    override fun encode(builder: PacketWriter, msg: TextTileMessage) {
        val (tile, duration, height, color, text) = msg
        builder.apply {
            writeOpcode(TILE_TEXT, PacketType.BYTE)
            writeByte(0)
            writeByte(tile)
            writeShort(duration)
            writeByte(height)
            writeMedium(color)
            writeString(text)
        }
    }
}
package rs.dusk.network.rs.codec.game.encode

import rs.dusk.buffer.write.writeString
import rs.dusk.core.network.codec.message.Encoder
import rs.dusk.core.network.codec.packet.PacketSize
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.rs.codec.game.GameOpcodes.TILE_TEXT

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since June 27, 2020
 */
class TextTileEncoder : Encoder(TILE_TEXT, PacketSize.BYTE) {

    fun encode(
        player: Player,
        tile: Int,
        duration: Int,
        height: Int,
        color: Int,
        text: String
    ) = player.send(8 + string(text)) {
        writeByte(0)
        writeByte(tile)
        writeShort(duration)
        writeByte(height)
        writeMedium(color)
        writeString(text)
    }
}
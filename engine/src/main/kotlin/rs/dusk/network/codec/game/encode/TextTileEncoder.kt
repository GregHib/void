package rs.dusk.network.codec.game.encode

import rs.dusk.buffer.write.writeString
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.codec.Encoder
import rs.dusk.network.codec.game.GameOpcodes.TILE_TEXT
import rs.dusk.network.packet.PacketSize

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
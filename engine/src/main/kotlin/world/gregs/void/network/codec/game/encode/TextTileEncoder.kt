package world.gregs.void.network.codec.game.encode

import world.gregs.void.buffer.write.writeString
import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.network.codec.Encoder
import world.gregs.void.network.codec.game.GameOpcodes.TILE_TEXT
import world.gregs.void.network.packet.PacketSize

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
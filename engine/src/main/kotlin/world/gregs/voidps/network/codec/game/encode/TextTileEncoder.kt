package world.gregs.voidps.network.codec.game.encode

import world.gregs.voidps.buffer.write.writeString
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.codec.Encoder
import world.gregs.voidps.network.codec.game.GameOpcodes.TILE_TEXT
import world.gregs.voidps.network.packet.PacketSize

/**
 * @author GregHib <greg@gregs.world>
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
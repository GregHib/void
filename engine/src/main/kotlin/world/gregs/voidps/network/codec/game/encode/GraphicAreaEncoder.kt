package world.gregs.voidps.network.codec.game.encode

import world.gregs.voidps.buffer.Modifier
import world.gregs.voidps.buffer.write.writeByte
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.codec.Encoder
import world.gregs.voidps.network.codec.game.GameOpcodes.GRAPHIC_AREA

/**
 * @author GregHib <greg@gregs.world>
 * @since June 27, 2020
 */
class GraphicAreaEncoder : Encoder(GRAPHIC_AREA) {

    /**
     * @param tile The tile offset from the chunk update send
     * @param id graphic id
     * @param height 0..255 start height off the ground
     * @param delay delay to start graphic 30 = 1 tick
     * @param rotation 0..7
     */
    fun encode(
        player: Player,
        tile: Int,
        id: Int,
        height: Int,
        delay: Int,
        rotation: Int
    ) = player.send(5, flush = false) {
        writeByte(tile)
        writeShort(id)
        writeByte(height)
        writeShort(delay)
        writeByte(rotation)
    }
}
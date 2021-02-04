package world.gregs.voidps.network.codec.game.encode

import world.gregs.voidps.buffer.Modifier
import world.gregs.voidps.buffer.write.writeByte
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.codec.Encoder
import world.gregs.voidps.network.codec.game.GameOpcodes.UPDATE_CHUNK

/**
 * @author GregHib <greg@gregs.world>
 * @since June 19, 2020
 */
class ChunkUpdateEncoder : Encoder(UPDATE_CHUNK) {

    /**
     * @param xOffset The chunk x coordinate relative to viewport
     * @param yOffset The chunk y coordinate relative to viewport
     * @param plane The chunks plane
     */
    fun encode(
        player: Player,
        flush: Boolean,
        xOffset: Int,
        yOffset: Int,
        plane: Int
    ) = player.send(3, flush = flush) {
        writeByte(yOffset, Modifier.INVERSE)
        writeByte(plane, Modifier.ADD)
        writeByte(xOffset, Modifier.ADD)
    }
}
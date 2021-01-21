package world.gregs.void.network.codec.game.encode

import world.gregs.void.buffer.Modifier
import world.gregs.void.buffer.write.writeByte
import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.network.codec.Encoder
import world.gregs.void.network.codec.game.GameOpcodes.CHUNK_CLEAR

/**
 * @author GregHib <greg@gregs.world>
 * @since June 21, 2020
 */
class ChunkClearEncoder : Encoder(CHUNK_CLEAR) {

    /**
     * @param xOffset The chunk x coordinate relative to viewport
     * @param yOffset The chunk y coordinate relative to viewport
     * @param plane The chunks plane
     */
    fun encode(
        player: Player,
        xOffset: Int,
        yOffset: Int,
        plane: Int
    ) = player.send(3) {
        writeByte(xOffset)
        writeByte(yOffset, Modifier.SUBTRACT)
        writeByte(plane)
    }
}
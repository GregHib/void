package world.gregs.voidps.network.codec.game.encode

import world.gregs.voidps.buffer.write.writeByteAdd
import world.gregs.voidps.buffer.write.writeByteInverse
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.codec.Encoder
import world.gregs.voidps.network.codec.game.GameOpcodes.CHUNK_CLEAR

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
        writeByteAdd(plane)
        writeByteInverse(yOffset)
        writeByteInverse(xOffset)
    }
}
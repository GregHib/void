package rs.dusk.network.rs.codec.game.encode

import rs.dusk.buffer.Modifier
import rs.dusk.buffer.write.writeByte
import rs.dusk.core.network.codec.message.Encoder
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.rs.codec.game.GameOpcodes.CHUNK_CLEAR

/**
 * @author Greg Hibberd <greg@greghibberd.com>
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
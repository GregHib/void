package rs.dusk.network.rs.codec.game.encode

import rs.dusk.buffer.Modifier
import rs.dusk.buffer.write.writeByte
import rs.dusk.core.network.codec.message.Encoder
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.rs.codec.game.GameOpcodes.UPDATE_CHUNK

/**
 * @author Greg Hibberd <greg@greghibberd.com>
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
        writeByte(xOffset, Modifier.ADD)
        writeByte(yOffset)
        writeByte(plane, type = Modifier.SUBTRACT)
    }
}
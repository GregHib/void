package rs.dusk.network.rs.codec.game.encode

import rs.dusk.buffer.Modifier
import rs.dusk.buffer.write.writeByte
import rs.dusk.core.network.codec.message.Encoder
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.rs.codec.game.GameOpcodes.OBJECT_REMOVE

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since June 27, 2020
 */
class ObjectRemoveEncoder : Encoder(OBJECT_REMOVE) {

    /**
     * @param tile The tile offset from the chunk update send
     * @param type Object type
     * @param rotation Object rotation
     */
    fun encode(
        player: Player,
        tile: Int,
        type: Int,
        rotation: Int
    ) = player.send(2, flush = false) {
        writeByte(tile, type = Modifier.INVERSE)
        writeByte((type shl 2) or rotation)
    }
}
package world.gregs.void.network.codec.game.encode

import world.gregs.void.buffer.Modifier
import world.gregs.void.buffer.write.writeByte
import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.network.codec.Encoder
import world.gregs.void.network.codec.game.GameOpcodes.OBJECT_REMOVE

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
package world.gregs.void.network.codec.game.encode

import world.gregs.void.buffer.Endian
import world.gregs.void.buffer.Modifier
import world.gregs.void.buffer.write.writeByte
import world.gregs.void.buffer.write.writeShort
import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.network.codec.Encoder
import world.gregs.void.network.codec.game.GameOpcodes.OBJECT_ANIMATION_SPECIFIC

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since June 27, 2020
 */
class ObjectAnimationSpecificEncoder : Encoder(OBJECT_ANIMATION_SPECIFIC) {

    /**
     * @param tile The tile offset from the chunk update send
     * @param animation Animation id
     * @param type Object type
     * @param rotation Object rotation
     */
    fun encode(
        player: Player,
        tile: Int,
        animation: Int,
        type: Int,
        rotation: Int
    ) = player.send(4, flush = false) {
        writeShort(animation, type = Modifier.ADD, order = Endian.LITTLE)
        writeByte(tile, type = Modifier.ADD)
        writeByte((type shl 2) or rotation)
    }
}
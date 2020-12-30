package rs.dusk.network.rs.codec.game.encode

import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.buffer.write.writeByte
import rs.dusk.buffer.write.writeShort
import rs.dusk.core.network.codec.message.Encoder
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.rs.codec.game.GameOpcodes.OBJECT_ANIMATION_SPECIFIC

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
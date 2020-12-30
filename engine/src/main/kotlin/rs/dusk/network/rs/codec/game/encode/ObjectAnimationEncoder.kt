package rs.dusk.network.rs.codec.game.encode

import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.buffer.write.writeByte
import rs.dusk.buffer.write.writeInt
import rs.dusk.buffer.write.writeShort
import rs.dusk.core.network.codec.message.Encoder
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.rs.codec.game.GameOpcodes.OBJECT_ANIMATION

/**
 * Show animation of an object for a single client
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since June 27, 2020
 */
class ObjectAnimationEncoder : Encoder(OBJECT_ANIMATION) {

    /**
     * @param tile 30 bit location hash
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
    ) = player.send(7) {
        writeInt(tile, order = Endian.MIDDLE, type = Modifier.INVERSE)
        writeShort(animation, type = Modifier.ADD)
        writeByte((type shl 2) or rotation, type = Modifier.SUBTRACT)
    }
}
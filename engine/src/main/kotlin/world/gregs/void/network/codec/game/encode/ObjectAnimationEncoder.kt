package world.gregs.void.network.codec.game.encode

import world.gregs.void.buffer.Endian
import world.gregs.void.buffer.Modifier
import world.gregs.void.buffer.write.writeByte
import world.gregs.void.buffer.write.writeInt
import world.gregs.void.buffer.write.writeShort
import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.network.codec.Encoder
import world.gregs.void.network.codec.game.GameOpcodes.OBJECT_ANIMATION

/**
 * Show animation of an object for a single client
 * @author GregHib <greg@gregs.world>
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
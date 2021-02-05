package world.gregs.voidps.network.codec.game.encode

import world.gregs.voidps.buffer.Endian
import world.gregs.voidps.buffer.Modifier
import world.gregs.voidps.buffer.write.writeByte
import world.gregs.voidps.buffer.write.writeInt
import world.gregs.voidps.buffer.write.writeShort
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.codec.Encoder
import world.gregs.voidps.network.codec.game.GameOpcodes.OBJECT_ANIMATION

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
        writeShort(animation, Modifier.ADD, Endian.LITTLE)
        writeByte((type shl 2) or rotation, Modifier.ADD)
        writeInt(tile, Modifier.INVERSE, Endian.MIDDLE)
    }
}
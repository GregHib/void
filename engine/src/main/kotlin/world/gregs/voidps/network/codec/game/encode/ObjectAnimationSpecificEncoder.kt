package world.gregs.voidps.network.codec.game.encode

import world.gregs.voidps.buffer.Endian
import world.gregs.voidps.buffer.Modifier
import world.gregs.voidps.buffer.write.writeByte
import world.gregs.voidps.buffer.write.writeShort
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.codec.Encoder
import world.gregs.voidps.network.codec.game.GameOpcodes.OBJECT_ANIMATION_SPECIFIC

/**
 * @author GregHib <greg@gregs.world>
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
        writeShort(animation, order = Endian.LITTLE)
        writeByte(tile, Modifier.SUBTRACT)
        writeByte((type shl 2) or rotation, Modifier.INVERSE)
    }
}
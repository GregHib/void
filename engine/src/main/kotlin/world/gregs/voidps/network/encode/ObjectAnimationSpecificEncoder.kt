package world.gregs.voidps.network.encode

import world.gregs.voidps.buffer.write.writeByteInverse
import world.gregs.voidps.buffer.write.writeByteSubtract
import world.gregs.voidps.buffer.write.writeShortLittle
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Encoder
import world.gregs.voidps.network.GameOpcodes.OBJECT_ANIMATION_SPECIFIC

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
        writeShortLittle(animation)
        writeByteSubtract(tile)
        writeByteInverse((type shl 2) or rotation)
    }
}
package world.gregs.voidps.network.codec.game.encode

import world.gregs.voidps.buffer.Modifier
import world.gregs.voidps.buffer.write.writeByte
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.codec.Encoder
import world.gregs.voidps.network.codec.game.GameOpcodes.OBJECT_REMOVE

/**
 * @author GregHib <greg@gregs.world>
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
        writeByte((type shl 2) or rotation, Modifier.ADD)
        writeByte(tile)
    }
}
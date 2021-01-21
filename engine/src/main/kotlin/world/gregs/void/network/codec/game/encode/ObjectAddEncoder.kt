package world.gregs.void.network.codec.game.encode

import world.gregs.void.buffer.Modifier
import world.gregs.void.buffer.write.writeShort
import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.network.codec.Encoder
import world.gregs.void.network.codec.game.GameOpcodes.OBJECT_ADD

/**
 * @author GregHib <greg@gregs.world>
 * @since June 27, 2020
 */
class ObjectAddEncoder : Encoder(OBJECT_ADD) {

    /**
     * @param tile The tile offset from the chunk update send
     * @param id Object id
     * @param type Object type
     * @param rotation Object rotation
     */
    fun encode(
        player: Player,
        tile: Int,
        id: Int,
        type: Int,
        rotation: Int
    ) = player.send(4, flush = false) {
        writeByte(tile)
        writeByte((type shl 2) or rotation)
        writeShort(id, type = Modifier.ADD)
    }
}
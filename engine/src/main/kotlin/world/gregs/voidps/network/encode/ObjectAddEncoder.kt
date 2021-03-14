package world.gregs.voidps.network.encode

import world.gregs.voidps.buffer.write.writeByteAdd
import world.gregs.voidps.buffer.write.writeByteSubtract
import world.gregs.voidps.buffer.write.writeShort
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Encoder
import world.gregs.voidps.network.GameOpcodes.OBJECT_ADD

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
        writeByteSubtract((type shl 2) or rotation)
        writeShort(id)
        writeByteAdd(tile)
    }
}
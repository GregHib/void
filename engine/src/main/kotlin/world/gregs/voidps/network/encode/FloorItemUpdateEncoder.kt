package world.gregs.voidps.network.encode

import world.gregs.voidps.buffer.write.writeByte
import world.gregs.voidps.buffer.write.writeShort
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Encoder
import world.gregs.voidps.network.GameOpcodes.FLOOR_ITEM_UPDATE

/**
 * @author GregHib <greg@gregs.world>
 * @since June 19, 2020
 */
class FloorItemUpdateEncoder : Encoder(FLOOR_ITEM_UPDATE) {

    /**
     * @author GregHib <greg@gregs.world>
     * @since June 19, 2020
     * @param tile The tile offset from the chunk update send
     * @param id Item id
     * @param oldAmount Previous item stack size
     * @param newAmount Updated item stack size
     */
    fun encode(
        player: Player,
        tile: Int,
        id: Int,
        oldAmount: Int,
        newAmount: Int
    ) = player.send(7, flush = false) {
        writeByte(tile)
        writeShort(id)
        writeShort(oldAmount)
        writeShort(newAmount)
    }
}
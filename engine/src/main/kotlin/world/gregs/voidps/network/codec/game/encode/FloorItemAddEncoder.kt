package world.gregs.voidps.network.codec.game.encode

import world.gregs.voidps.buffer.write.writeByte
import world.gregs.voidps.buffer.write.writeShortLittle
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.codec.Encoder
import world.gregs.voidps.network.codec.game.GameOpcodes.FLOOR_ITEM_ADD

/**
 * @author GregHib <greg@gregs.world>
 * @since June 19, 2020
 */
class FloorItemAddEncoder : Encoder(FLOOR_ITEM_ADD) {

    /**
     * @param tile The tile offset from the chunk update send
     * @param id Item id
     * @param amount Item stack size
     */
    fun encode(
        player: Player,
        tile: Int,
        id: Int,
        amount: Int
    ) = player.send(5, flush = false) {
        writeShortLittle(amount)
        writeShortLittle(id)
        writeByte(tile)
    }
}
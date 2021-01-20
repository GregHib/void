package world.gregs.void.network.codec.game.encode

import world.gregs.void.buffer.Modifier
import world.gregs.void.buffer.write.writeByte
import world.gregs.void.buffer.write.writeShort
import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.network.codec.Encoder
import world.gregs.void.network.codec.game.GameOpcodes.FLOOR_ITEM_ADD

/**
 * @author Greg Hibberd <greg@greghibberd.com>
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
        writeByte(tile, type = Modifier.INVERSE)
        writeShort(id, type = Modifier.ADD)
        writeShort(amount)
    }
}
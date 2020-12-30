package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.network.codec.message.Encoder
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.rs.codec.game.GameOpcodes.FLOOR_ITEM_UPDATE

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since June 19, 2020
 */
class FloorItemUpdateEncoder : Encoder(FLOOR_ITEM_UPDATE) {

    /**
     * @author Greg Hibberd <greg@greghibberd.com>
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
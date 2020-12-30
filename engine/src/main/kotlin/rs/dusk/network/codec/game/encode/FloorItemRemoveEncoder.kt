package rs.dusk.network.codec.game.encode

import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.codec.Encoder
import rs.dusk.network.codec.game.GameOpcodes.FLOOR_ITEM_REMOVE

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since June 19, 2020
 */
class FloorItemRemoveEncoder : Encoder(FLOOR_ITEM_REMOVE) {

    /**
     * @param tile The tile offset from the chunk update send
     * @param id Item id
     */
    fun encode(
        player: Player,
        tile: Int,
        id: Int
    ) = player.send(3, flush = false) {
        writeShort(id)
        writeByte(tile)
    }
}
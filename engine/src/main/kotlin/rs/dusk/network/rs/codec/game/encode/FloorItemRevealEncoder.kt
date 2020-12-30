package rs.dusk.network.rs.codec.game.encode

import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.buffer.write.writeByte
import rs.dusk.buffer.write.writeShort
import rs.dusk.core.network.codec.message.Encoder
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.rs.codec.game.GameOpcodes.FLOOR_ITEM_REVEAL

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since June 19, 2020
 */
class FloorItemRevealEncoder : Encoder(FLOOR_ITEM_REVEAL) {

    /**
     * @param tile The tile offset from the chunk update send
     * @param id Item id
     * @param amount Item stack size
     * @param owner Client index if matches client's local index then item won't be displayed
     */
    fun encode(
        player: Player,
        tile: Int,
        id: Int,
        amount: Int,
        owner: Int
    ) = player.send(7, flush = false) {
        writeShort(owner, type = Modifier.ADD)
        writeByte(tile, type = Modifier.ADD)
        writeShort(id, order = Endian.LITTLE)
        writeShort(amount)
    }
}
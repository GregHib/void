package world.gregs.voidps.network.codec.game.encode

import world.gregs.voidps.buffer.Endian
import world.gregs.voidps.buffer.Modifier
import world.gregs.voidps.buffer.write.writeByte
import world.gregs.voidps.buffer.write.writeShort
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.codec.Encoder
import world.gregs.voidps.network.codec.game.GameOpcodes.FLOOR_ITEM_REVEAL

/**
 * @author GregHib <greg@gregs.world>
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
        writeShort(amount, order = Endian.LITTLE)
        writeByte(tile)
        writeShort(id, Modifier.ADD)
        writeShort(owner, Modifier.ADD)
    }
}
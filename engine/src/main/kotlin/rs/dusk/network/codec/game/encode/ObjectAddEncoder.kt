package rs.dusk.network.codec.game.encode

import rs.dusk.buffer.Modifier
import rs.dusk.buffer.write.writeShort
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.codec.Encoder
import rs.dusk.network.codec.game.GameOpcodes.OBJECT_ADD

/**
 * @author Greg Hibberd <greg@greghibberd.com>
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
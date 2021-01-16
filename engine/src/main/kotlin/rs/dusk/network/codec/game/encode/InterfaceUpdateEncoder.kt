package rs.dusk.network.codec.game.encode

import rs.dusk.buffer.Modifier
import rs.dusk.buffer.write.writeByte
import rs.dusk.buffer.write.writeShort
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.codec.Encoder
import rs.dusk.network.codec.game.GameOpcodes.INTERFACE_WINDOW

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 18, 2020
 */
class InterfaceUpdateEncoder : Encoder(INTERFACE_WINDOW) {

    fun encode(
        player: Player,
        id: Int,
        type: Int
    ) = player.send(3) {
        writeByte(type, Modifier.INVERSE)
        writeShort(id, Modifier.ADD)
    }
}
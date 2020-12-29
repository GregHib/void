package rs.dusk.network.rs.codec.game.encode

import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.buffer.write.writeByte
import rs.dusk.buffer.write.writeShort
import rs.dusk.core.network.codec.message.MessageEncoder
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.rs.codec.game.GameOpcodes.INTERFACE_WINDOW

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 18, 2020
 */
class InterfaceUpdateMessageEncoder : MessageEncoder(INTERFACE_WINDOW) {

    fun encode(
        player: Player,
        id: Int,
        type: Int
    ) = player.send(3) {
        writeShort(id, Modifier.ADD, Endian.LITTLE)
        writeByte(type, Modifier.SUBTRACT)
    }
}
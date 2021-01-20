package world.gregs.void.network.codec.game.encode

import world.gregs.void.buffer.Endian
import world.gregs.void.buffer.Modifier
import world.gregs.void.buffer.write.writeByte
import world.gregs.void.buffer.write.writeShort
import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.network.codec.Encoder
import world.gregs.void.network.codec.game.GameOpcodes.INTERFACE_WINDOW

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
        writeShort(id, Modifier.ADD, Endian.LITTLE)
        writeByte(type, Modifier.SUBTRACT)
    }
}
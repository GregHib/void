package rs.dusk.network.codec.game.encode

import rs.dusk.buffer.Endian
import rs.dusk.buffer.write.writeInt
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.codec.Encoder
import rs.dusk.network.codec.game.GameOpcodes.INTERFACE_CLOSE

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since July 25, 2020
 */
class InterfaceCloseEncoder : Encoder(INTERFACE_CLOSE) {

    /**
     * Closes a client interface
     * @param id The id of the parent interface
     * @param component The index of the component to close
     */
    fun encode(
        player: Player,
        id: Int,
        component: Int
    ) = player.send(4) {
        writeInt(id shl 16 or component, order = Endian.LITTLE)
    }
}
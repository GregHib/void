package rs.dusk.network.rs.codec.game.encode

import rs.dusk.buffer.Endian
import rs.dusk.buffer.write.writeInt
import rs.dusk.core.network.codec.message.MessageEncoder
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.rs.codec.game.GameOpcodes.INTERFACE_PLAYER_HEAD

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since August 2, 2020
 */
class InterfaceHeadPlayerMessageEncoder : MessageEncoder(INTERFACE_PLAYER_HEAD) {

    /**
     * Sends command to display the players head on a interface component
     * @param id The id of the parent interface
     * @param component The index of the component
     */
    fun encode(
        player: Player,
        id: Int,
        component: Int
    ) = player.send(4) {
        writeInt(id shl 16 or component, order = Endian.LITTLE)
    }
}
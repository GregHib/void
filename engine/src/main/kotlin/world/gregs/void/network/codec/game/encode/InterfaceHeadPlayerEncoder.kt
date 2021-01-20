package world.gregs.void.network.codec.game.encode

import world.gregs.void.buffer.Endian
import world.gregs.void.buffer.write.writeInt
import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.network.codec.Encoder
import world.gregs.void.network.codec.game.GameOpcodes.INTERFACE_PLAYER_HEAD

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since August 2, 2020
 */
class InterfaceHeadPlayerEncoder : Encoder(INTERFACE_PLAYER_HEAD) {

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
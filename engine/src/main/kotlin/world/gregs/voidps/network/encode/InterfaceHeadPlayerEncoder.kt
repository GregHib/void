package world.gregs.voidps.network.encode

import world.gregs.voidps.buffer.write.writeIntMiddle
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Encoder
import world.gregs.voidps.network.GameOpcodes.INTERFACE_PLAYER_HEAD

/**
 * @author GregHib <greg@gregs.world>
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
        writeIntMiddle(id shl 16 or component)
    }
}
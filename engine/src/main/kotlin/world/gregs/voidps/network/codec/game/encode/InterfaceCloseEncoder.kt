package world.gregs.voidps.network.codec.game.encode

import world.gregs.voidps.buffer.Endian
import world.gregs.voidps.buffer.write.writeInt
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.codec.Encoder
import world.gregs.voidps.network.codec.game.GameOpcodes.INTERFACE_CLOSE

/**
 * @author GregHib <greg@gregs.world>
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
        writeInt(id shl 16 or component)
    }
}
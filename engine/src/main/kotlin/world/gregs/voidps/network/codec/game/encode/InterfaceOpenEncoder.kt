package world.gregs.voidps.network.codec.game.encode

import world.gregs.voidps.buffer.Endian
import world.gregs.voidps.buffer.Modifier
import world.gregs.voidps.buffer.write.writeByte
import world.gregs.voidps.buffer.write.writeInt
import world.gregs.voidps.buffer.write.writeShort
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.codec.Encoder
import world.gregs.voidps.network.codec.game.GameOpcodes.INTERFACE_OPEN

/**
 * @author GregHib <greg@gregs.world>
 * @since July 25, 2020
 */
class InterfaceOpenEncoder : Encoder(INTERFACE_OPEN) {

    /**
     * Displays a interface onto the client screen
     * @param permanent Whether the interface should be removed on player movement
     * @param parent The id of the parent interface
     * @param component The index of the component
     * @param id The id of the interface to display
     */
    fun encode(
        player: Player,
        permanent: Boolean,
        parent: Int,
        component: Int,
        id: Int
    ) = player.send(7) {
        writeShort(id, order = Endian.LITTLE)
        writeInt(parent shl 16 or component, order = Endian.LITTLE)
        writeByte(permanent, Modifier.ADD)
    }
}
package world.gregs.voidps.network.encode

import world.gregs.voidps.buffer.write.writeByteAdd
import world.gregs.voidps.buffer.write.writeIntLittle
import world.gregs.voidps.buffer.write.writeShortLittle
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Encoder
import world.gregs.voidps.network.GameOpcodes.INTERFACE_OPEN

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
        writeShortLittle(id)
        writeIntLittle(parent shl 16 or component)
        writeByteAdd(permanent)
    }
}
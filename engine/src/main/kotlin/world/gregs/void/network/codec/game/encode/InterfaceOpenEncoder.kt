package world.gregs.void.network.codec.game.encode

import world.gregs.void.buffer.Endian
import world.gregs.void.buffer.Modifier
import world.gregs.void.buffer.write.writeByte
import world.gregs.void.buffer.write.writeInt
import world.gregs.void.buffer.write.writeShort
import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.network.codec.Encoder
import world.gregs.void.network.codec.game.GameOpcodes.INTERFACE_OPEN

/**
 * @author Greg Hibberd <greg@greghibberd.com>
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
        writeShort(id, Modifier.ADD, Endian.LITTLE)
        writeInt(parent shl 16 or component, order = Endian.LITTLE)
        writeByte(permanent)
    }
}
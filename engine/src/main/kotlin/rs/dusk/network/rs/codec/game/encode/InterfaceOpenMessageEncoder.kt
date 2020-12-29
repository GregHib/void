package rs.dusk.network.rs.codec.game.encode

import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.buffer.write.writeByte
import rs.dusk.buffer.write.writeInt
import rs.dusk.buffer.write.writeShort
import rs.dusk.core.network.codec.message.MessageEncoder
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.rs.codec.game.GameOpcodes.INTERFACE_OPEN

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since July 25, 2020
 */
class InterfaceOpenMessageEncoder : MessageEncoder(INTERFACE_OPEN) {

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
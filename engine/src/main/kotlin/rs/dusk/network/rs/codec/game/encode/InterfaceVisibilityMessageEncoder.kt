package rs.dusk.network.rs.codec.game.encode

import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.buffer.write.writeByte
import rs.dusk.buffer.write.writeInt
import rs.dusk.core.network.codec.message.MessageEncoder
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.rs.codec.game.GameOpcodes.INTERFACE_COMPONENT_VISIBILITY

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since August 2, 2020
 */
class InterfaceVisibilityMessageEncoder : MessageEncoder(INTERFACE_COMPONENT_VISIBILITY) {

    /**
     * Toggles a interface component
     * @param id The parent interface id
     * @param component The component to change
     * @param hide Visibility
     */
    fun encode(
        player: Player,
        id: Int,
        component: Int,
        hide: Boolean
    ) = player.send(5) {
        writeInt(id shl 16 or component, order = Endian.MIDDLE)
        writeByte(hide, Modifier.ADD)
    }
}
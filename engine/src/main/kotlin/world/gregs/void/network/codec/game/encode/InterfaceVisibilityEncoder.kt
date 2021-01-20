package world.gregs.void.network.codec.game.encode

import world.gregs.void.buffer.Endian
import world.gregs.void.buffer.Modifier
import world.gregs.void.buffer.write.writeByte
import world.gregs.void.buffer.write.writeInt
import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.network.codec.Encoder
import world.gregs.void.network.codec.game.GameOpcodes.INTERFACE_COMPONENT_VISIBILITY

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since August 2, 2020
 */
class InterfaceVisibilityEncoder : Encoder(INTERFACE_COMPONENT_VISIBILITY) {

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
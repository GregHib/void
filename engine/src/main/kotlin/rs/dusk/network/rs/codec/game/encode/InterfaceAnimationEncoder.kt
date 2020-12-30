package rs.dusk.network.rs.codec.game.encode

import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.buffer.write.writeInt
import rs.dusk.buffer.write.writeShort
import rs.dusk.core.network.codec.message.Encoder
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.rs.codec.game.GameOpcodes.INTERFACE_ANIMATION

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since August 02, 2020
 */
class InterfaceAnimationEncoder : Encoder(INTERFACE_ANIMATION) {

    /**
     * Sends an animation to a interface component
     * @param id The id of the parent interface
     * @param component The index of the component
     * @param animation The animation id
     */
    fun encode(
        player: Player,
        id: Int,
        component: Int,
        animation: Int
    ) = player.send(6) {
        writeShort(animation, Modifier.ADD, Endian.LITTLE)
        writeInt(id shl 16 or component, order = Endian.MIDDLE)
    }
}
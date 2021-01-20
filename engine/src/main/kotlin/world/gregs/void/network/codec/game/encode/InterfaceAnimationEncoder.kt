package world.gregs.void.network.codec.game.encode

import world.gregs.void.buffer.Endian
import world.gregs.void.buffer.Modifier
import world.gregs.void.buffer.write.writeInt
import world.gregs.void.buffer.write.writeShort
import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.network.codec.Encoder
import world.gregs.void.network.codec.game.GameOpcodes.INTERFACE_ANIMATION

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
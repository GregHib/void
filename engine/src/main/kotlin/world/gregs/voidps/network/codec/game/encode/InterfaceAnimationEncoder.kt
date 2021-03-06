package world.gregs.voidps.network.codec.game.encode

import world.gregs.voidps.buffer.write.writeIntMiddle
import world.gregs.voidps.buffer.write.writeShort
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.codec.Encoder
import world.gregs.voidps.network.codec.game.GameOpcodes.INTERFACE_ANIMATION

/**
 * @author GregHib <greg@gregs.world>
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
        writeShort(animation)
        writeIntMiddle(id shl 16 or component)
    }
}
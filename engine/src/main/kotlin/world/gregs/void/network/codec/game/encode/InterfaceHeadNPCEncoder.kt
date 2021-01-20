package world.gregs.void.network.codec.game.encode

import world.gregs.void.buffer.Endian
import world.gregs.void.buffer.write.writeShort
import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.network.codec.Encoder
import world.gregs.void.network.codec.game.GameOpcodes.INTERFACE_NPC_HEAD

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since August 2, 2020
 */
class InterfaceHeadNPCEncoder : Encoder(INTERFACE_NPC_HEAD) {

    /**
     * Sends npc who's head to display on a interface component
     * @param id The id of the parent interface
     * @param component The index of the component
     * @param npc The id of the npc
     */
    fun encode(
        player: Player,
        id: Int,
        component: Int,
        npc: Int
    ) = player.send(6) {
        writeInt(id shl 16 or component)
        writeShort(npc, order = Endian.LITTLE)
    }
}
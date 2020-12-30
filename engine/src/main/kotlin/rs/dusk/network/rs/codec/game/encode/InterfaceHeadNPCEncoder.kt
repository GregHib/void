package rs.dusk.network.rs.codec.game.encode

import rs.dusk.buffer.Endian
import rs.dusk.buffer.write.writeShort
import rs.dusk.core.network.codec.message.Encoder
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.rs.codec.game.GameOpcodes.INTERFACE_NPC_HEAD

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
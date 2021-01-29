package world.gregs.voidps.network.codec.game.encode

import world.gregs.voidps.buffer.Endian
import world.gregs.voidps.buffer.Modifier
import world.gregs.voidps.buffer.write.writeInt
import world.gregs.voidps.buffer.write.writeShort
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.codec.Encoder
import world.gregs.voidps.network.codec.game.GameOpcodes.INTERFACE_NPC_HEAD

/**
 * @author GregHib <greg@gregs.world>
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
        writeInt(id shl 16 or component, order = Endian.LITTLE)
        writeShort(npc, Modifier.ADD)
    }
}
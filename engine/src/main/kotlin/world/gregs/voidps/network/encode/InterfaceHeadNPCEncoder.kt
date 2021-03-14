package world.gregs.voidps.network.encode

import world.gregs.voidps.buffer.write.writeIntLittle
import world.gregs.voidps.buffer.write.writeShortAdd
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Encoder
import world.gregs.voidps.network.GameOpcodes.INTERFACE_NPC_HEAD

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
        writeIntLittle(id shl 16 or component)
        writeShortAdd(npc)
    }
}
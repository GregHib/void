package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message
import rs.dusk.network.rs.codec.game.MessageCompanion

/**
 * An option selection on a npc
 * @param run Whether the player should force run
 * @param npcIndex The npc client index
 * @param option The option id - 2 = Attack, 6 = Examine
 */
data class NPCOptionMessage(val run: Boolean, val npcIndex: Int, val option: Int) : Message {
    companion object : MessageCompanion<NPCOptionMessage>()
}
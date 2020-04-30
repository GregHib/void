package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message
import rs.dusk.network.rs.codec.game.MessageCompanion

/**
 * Interface container action applied to a npc
 * @param slot The interface item slot
 * @param type The interface item type
 * @param npc The npc client index
 * @param hash The interface and component id
 * @param run Force run
 */
data class InterfaceOnNpcMessage(val slot: Int, val type: Int, val npc: Int, val hash: Int, val run: Boolean) :
    Message {
    companion object : MessageCompanion<InterfaceOnNpcMessage>()
}

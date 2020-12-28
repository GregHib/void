package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message
import rs.dusk.network.rs.codec.game.MessageCompanion

/**
 * Interface container action applied to a player
 * @param player The player index to apply on
 * @param hash The interface and component id
 * @param type The interface item type
 * @param run Force run
 * @param slot The component item slot
 */
data class InterfaceOnPlayerMessage(
    val player: Int,
    val hash: Int,
    val type: Int,
    val run: Boolean,
    val slot: Int
) : Message {
    companion object : MessageCompanion<InterfaceOnPlayerMessage>()
}
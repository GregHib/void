package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message
import rs.dusk.network.rs.codec.game.MessageCompanion

/**
 * Interface container action applied to an object
 * @param run Force run
 * @param y The objects y coordinate
 * @param slot The interface item slot
 * @param hash The interface and component id
 * @param type The item id
 * @param y The objects y coordinate
 * @param id The objects id
 */
data class InterfaceOnObjectMessage(
    val run: Boolean,
    val y: Int,
    val slot: Int,
    val hash: Int,
    val type: Int,
    val x: Int,
    val id: Int
) : Message {
    companion object : MessageCompanion<InterfaceOnObjectMessage>()
}
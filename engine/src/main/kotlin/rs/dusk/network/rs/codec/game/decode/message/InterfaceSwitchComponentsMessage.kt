package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message
import rs.dusk.network.rs.codec.game.MessageCompanion

/**
 * Action of one component dragged to another
 * @param toType The first item type
 * @param fromSlot The first item slot
 * @param fromType The second item type
 * @param fromHash The first interface and component ids hash
 * @param toSlot The second item slot
 * @param toHash The second interface and component ids hash
 */
data class InterfaceSwitchComponentsMessage(
    val toType: Int,
    val fromSlot: Int,
    val fromType: Int,
    val fromHash: Int,
    val toSlot: Int,
    val toHash: Int
) : Message {
    companion object : MessageCompanion<InterfaceSwitchComponentsMessage>()
}

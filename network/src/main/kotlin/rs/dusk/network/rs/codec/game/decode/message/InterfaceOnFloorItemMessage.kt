package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message
import rs.dusk.network.rs.codec.game.MessageCompanion

/**
 * Interface container action applied to a floor item
 * @param x The floor x coordinate
 * @param y The floor x coordinate
 * @param floorType The item type of the floor item
 * @param hash The interface id & component spell id hash
 * @param slot The interface item slot
 * @param run Force run
 * @param item The item type of the interface item
 */
data class InterfaceOnFloorItemMessage(
    val x: Int,
    val y: Int,
    val floorType: Int,
    val hash: Int,
    val slot: Int,
    val run: Boolean,
    val item: Int
) : Message {
    companion object : MessageCompanion<InterfaceOnFloorItemMessage>()
}
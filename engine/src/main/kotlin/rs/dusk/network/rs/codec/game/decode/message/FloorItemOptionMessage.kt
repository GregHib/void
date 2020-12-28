package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message
import rs.dusk.network.rs.codec.game.MessageCompanion

/**
 * An option selection on a floor item
 * @param id The item id
 * @param run Whether the player should force run
 * @param y The items y coordinate
 * @param x The items x coordinate
 * @param option The option id - 3 = Take
 */
data class FloorItemOptionMessage(val id: Int, val run: Boolean, val y: Int, val x: Int, val option: Int) : Message {
    companion object : MessageCompanion<FloorItemOptionMessage>()
}
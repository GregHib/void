package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message
import rs.dusk.network.rs.codec.game.MessageCompanion

/**
 * An option selection on an object
 * @param objectId The type id of the object selected
 * @param x The object's x coordinate
 * @param y The object's y coordinate
 * @param run Whether the player should force run
 * @param option The option id - 6 = Examine
 */
data class ObjectOptionMessage(val objectId: Int, val x: Int, val y: Int, val run: Boolean, val option: Int) : Message {
    companion object : MessageCompanion<ObjectOptionMessage>()
}
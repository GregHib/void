package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message
import rs.dusk.network.rs.codec.game.MessageCompanion

/**
 * Called when the users mouse enters or exits the client area
 * @param over Whether the mouse is over the client or not
 */
data class WindowHoveredMessage(val over: Boolean) : Message {
    companion object : MessageCompanion<WindowHoveredMessage>()
}
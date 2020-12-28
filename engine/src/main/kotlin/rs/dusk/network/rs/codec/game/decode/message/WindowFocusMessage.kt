package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message
import rs.dusk.network.rs.codec.game.MessageCompanion

/**
 * Called when the client window changes status
 * @param focused Whether the client is focused or not
 */
data class WindowFocusMessage(val focused: Boolean) : Message {
    companion object : MessageCompanion<WindowFocusMessage>()
}
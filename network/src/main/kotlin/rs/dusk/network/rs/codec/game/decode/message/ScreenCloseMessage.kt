package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message
import rs.dusk.network.rs.codec.game.MessageCompanion

/**
 * Notification that the player clicked an X button on a screen interface
 */
object ScreenCloseMessage : MessageCompanion<ScreenCloseMessage>(), Message
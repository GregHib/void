package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message
import rs.dusk.network.rs.codec.game.MessageCompanion

/**
 * Notification that the world map orb has been pressed
 */
class WorldMapCloseMessage : Message {
    companion object : MessageCompanion<WorldMapCloseMessage>()
}
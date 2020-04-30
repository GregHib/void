package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message
import rs.dusk.network.rs.codec.game.MessageCompanion

/**
 * Information entered in a enter string dialogue pop-up
 * @param text The string entered
 */
data class StringEntryMessage(val text: String) : Message {
    companion object : MessageCompanion<StringEntryMessage>()
}
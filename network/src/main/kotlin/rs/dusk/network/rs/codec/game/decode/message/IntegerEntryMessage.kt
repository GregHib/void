package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message
import rs.dusk.network.rs.codec.game.MessageCompanion

/**
 * An integer entered in the entry box dialogue
 * @param integer The value entered
 */
data class IntegerEntryMessage(val integer: Int) : Message {
    companion object : MessageCompanion<IntegerEntryMessage>()
}
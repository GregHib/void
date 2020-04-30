package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message
import rs.dusk.network.rs.codec.game.MessageCompanion

/**
 * @param value Some kind of colour value
 */
data class UnknownMessage(val value: Int) : Message {
    companion object : MessageCompanion<UnknownMessage>()
}
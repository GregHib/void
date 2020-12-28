package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message
import rs.dusk.network.rs.codec.game.MessageCompanion

/**
 * String from script 4701, game state must not be 7 and length must be less than or equal to 20
 * Might be kicking or banning from clan chat via interface
 * @param string Unknown value
 */
data class UnknownScriptMessage(val string: String) : Message {
    companion object : MessageCompanion<UnknownScriptMessage>()
}
package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message
import rs.dusk.network.rs.codec.game.MessageCompanion

/**
 * Requests a change to the players clans forum thread
 * @param string The clans forum thread
 */
data class ClanForumThreadMessage(val string: String) : Message {
    companion object : MessageCompanion<ClanForumThreadMessage>()
}
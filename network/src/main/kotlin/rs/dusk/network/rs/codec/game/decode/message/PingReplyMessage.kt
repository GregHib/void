package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message
import rs.dusk.network.rs.codec.game.MessageCompanion

/**
 * The two values sent the client by packet 19
 */
data class PingReplyMessage(val first: Int, val second: Int) : Message {
    companion object : MessageCompanion<PingReplyMessage>()
}
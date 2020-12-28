package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message
import rs.dusk.network.rs.codec.game.MessageCompanion

/**
 * Attempt to kick a clan mate
 * @param owner Whether is the players clan - aka myClan
 * @param equals Whether the name is a match
 * @param member The display name of the member to kick
 */
data class ClanChatKickMessage(val owner: Boolean, val equals: Int, val member: String) : Message {
    companion object : MessageCompanion<ClanChatKickMessage>()
}
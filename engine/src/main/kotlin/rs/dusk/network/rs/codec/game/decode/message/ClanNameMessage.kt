package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message
import rs.dusk.network.rs.codec.game.MessageCompanion

/**
 * Requests a change to the players clans name
 * @param name The new clan name
 */
data class ClanNameMessage(val name: String) : Message {
    companion object : MessageCompanion<ClanNameMessage>()
}
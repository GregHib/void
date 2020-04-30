package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message
import rs.dusk.network.rs.codec.game.MessageCompanion

/**
 * @param first Unknown value
 * @param string Unknown value
 */
data class ClanSettingsUpdateMessage(val first: Int, val string: String) : Message {
    companion object : MessageCompanion<ClanSettingsUpdateMessage>()
}
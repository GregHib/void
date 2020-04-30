package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message
import rs.dusk.network.rs.codec.game.MessageCompanion

/**
 * A command typed out in the client console
 * @param command The command sent by the player
 */
data class ConsoleCommandMessage(val command: String) : Message {
    companion object : MessageCompanion<ConsoleCommandMessage>()
}
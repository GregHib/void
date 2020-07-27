package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message

/**
 * Notified the type of message before a message is sent
 * @param type The type of message sent (0 = public, 1 = friends chat)
 */
data class ChatTypeMessage(val type: Int) : Message
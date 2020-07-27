package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message

/**
 * Private message sent to another player
 * @param name The friends display name
 * @param message The message sent
 */
data class PrivateMessage(val name: String, val message: String) : Message
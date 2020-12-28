package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message

/**
 * Public chat message
 * @param message The message sent
 * @param effects The colour and move effect combined
 */
data class PublicMessage(val message: String, val effects: Int) : Message
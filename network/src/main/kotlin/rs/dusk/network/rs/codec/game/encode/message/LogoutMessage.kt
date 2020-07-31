package rs.dusk.network.rs.codec.game.encode.message

import rs.dusk.core.network.model.message.Message

/**
 * Disconnect client
 * @param lobby Whether to attempt reconnect to lobby
 */
data class LogoutMessage(val lobby: Boolean) : Message
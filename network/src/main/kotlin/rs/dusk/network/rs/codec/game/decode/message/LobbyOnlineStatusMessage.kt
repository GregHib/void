package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message

/**
 * Player has changed their online status while in the lobby
 * @param first Unknown
 * @param status The players online status
 * @param second Unknown
 */
data class LobbyOnlineStatusMessage(val first: Int, val status: Int, val second: Int) : Message
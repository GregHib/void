package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message

/**
 * Player wants to join a friends chat
 * @param name The display name of the friend who's chat to join
 */
data class FriendChatJoinMessage(val name: String) : Message
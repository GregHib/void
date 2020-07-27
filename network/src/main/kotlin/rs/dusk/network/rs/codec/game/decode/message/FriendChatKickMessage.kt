package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message

/**
 * Player wants to kick a player from their friends chat
 * @param name The display name of the player to kick
 */
data class FriendChatKickMessage(val name: String) : Message
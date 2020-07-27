package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message

/**
 * Player wants to change the rank of a friend on their friend list
 * @param name The display name of the player who's rank to change
 * @param rank The rank to give their friend
 */
data class FriendChatRankMessage(val name: String, val rank: Int) : Message
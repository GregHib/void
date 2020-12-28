package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message

/**
 * Player wants to remove a player from their friend list
 * @param name The display name of the player to remove
 */
data class FriendListRemoveMessage(val name: String) : Message
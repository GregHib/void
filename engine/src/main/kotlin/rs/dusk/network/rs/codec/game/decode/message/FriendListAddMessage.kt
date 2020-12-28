package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message

/**
 * Player wants to add another player to their friend list
 * @param name The display name of the player to add
 */
data class FriendListAddMessage(val name: String) : Message
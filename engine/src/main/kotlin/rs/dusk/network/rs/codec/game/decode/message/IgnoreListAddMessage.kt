package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message

/**
 * Player wants to add a player to their ignore list
 * Note: temporary ignores optional after report abuse
 * @param name The display name of the player to add
 * @param temporary Whether the ignore will be removed after logout
 */
data class IgnoreListAddMessage(val name: String, val temporary: Boolean) : Message
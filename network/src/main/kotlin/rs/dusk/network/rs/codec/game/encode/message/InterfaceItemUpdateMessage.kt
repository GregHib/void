package rs.dusk.network.rs.codec.game.encode.message

import rs.dusk.core.network.model.message.Message

/**
 * Sends a list of items to display on a interface item group component
 * @param key The id of the interface item group
 * @param updates List of the indices, item ids and amounts to update
 * @param negativeKey Whether the key is negative and needs encoding differently (optional - calculated automatically)
 */
data class InterfaceItemUpdateMessage(val key: Int, val updates: List<Triple<Int, Int, Int>>, val negativeKey: Boolean = key < 0) : Message
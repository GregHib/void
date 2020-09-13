package rs.dusk.network.rs.codec.game.encode.message

import rs.dusk.core.network.model.message.Message

/**
 * Sends a list of items to display on a interface item group component
 * @param key The id of the interface item group
 * @param updates List of the indices, item ids and amounts to update
 * @param secondary Optional to send to the primary or secondary container
 */
data class ContainerItemUpdateMessage(val key: Int, val updates: List<Triple<Int, Int, Int>>, val secondary: Boolean) : Message
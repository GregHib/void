package rs.dusk.network.rs.codec.game.encode.message

import rs.dusk.core.network.model.message.Message

/**
 * Sends an item to display on a interface component
 * @param id The id of the parent interface
 * @param component The index of the component
 * @param item The item id
 * @param amount The number of the item
 */
data class InterfaceItemMessage(val id: Int, val component: Int, val item: Int, val amount: Int) : Message
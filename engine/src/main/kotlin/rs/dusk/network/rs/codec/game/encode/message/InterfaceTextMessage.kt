package rs.dusk.network.rs.codec.game.encode.message

import rs.dusk.core.network.model.message.Message

/**
 * Update the text of a interface component
 * @param id The id of the parent interface
 * @param component The index of the component
 * @param text The text to send
 */
data class InterfaceTextMessage(val id: Int, val component: Int, val text: String) : Message
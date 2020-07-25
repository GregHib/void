package rs.dusk.network.rs.codec.game.encode.message

import rs.dusk.core.network.model.message.Message

/**
 * Closes a client interface
 * @param id The id of the parent interface
 * @param component The index of the component to close
 */
data class InterfaceCloseMessage(val id: Int, val component: Int) : Message
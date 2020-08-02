package rs.dusk.network.rs.codec.game.encode.message

import rs.dusk.core.network.model.message.Message

/**
 * Sends command to display the players head on a interface component
 * @param id The id of the parent interface
 * @param component The index of the component
 */
data class InterfaceHeadPlayerMessage(val id: Int, val component: Int) : Message
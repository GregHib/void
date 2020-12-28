package rs.dusk.network.rs.codec.game.encode.message

import rs.dusk.core.network.model.message.Message

/**
 * Sends npc who's head to display on a interface component
 * @param id The id of the parent interface
 * @param component The index of the component
 * @param npc The id of the npc
 */
data class InterfaceHeadNPCMessage(val id: Int, val component: Int, val npc: Int) : Message
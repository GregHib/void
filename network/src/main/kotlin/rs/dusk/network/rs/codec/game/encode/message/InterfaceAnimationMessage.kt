package rs.dusk.network.rs.codec.game.encode.message

import rs.dusk.core.network.model.message.Message

/**
 * Sends an animation to a interface component
 * @param id The id of the parent interface
 * @param component The index of the component
 * @param animation The animation id
 */
data class InterfaceAnimationMessage(val id: Int, val component: Int, val animation: Int) : Message
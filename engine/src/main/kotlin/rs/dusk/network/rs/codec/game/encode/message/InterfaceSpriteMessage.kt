package rs.dusk.network.rs.codec.game.encode.message

import rs.dusk.core.network.model.message.Message

/**
 * Sends a sprite to a interface component
 * @param id The id of the parent interface
 * @param component The index of the component
 * @param sprite The sprite id
 */
data class InterfaceSpriteMessage(val id: Int, val component: Int, val sprite: Int) : Message
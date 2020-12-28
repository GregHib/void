package rs.dusk.network.rs.codec.game.encode.message

import rs.dusk.core.network.model.message.Message

/**
 * Toggles a interface component
 * @param id The parent interface id
 * @param component The component to change
 * @param hide Visibility
 */
data class InterfaceVisibilityMessage(val id: Int, val component: Int, val hide: Boolean) : Message
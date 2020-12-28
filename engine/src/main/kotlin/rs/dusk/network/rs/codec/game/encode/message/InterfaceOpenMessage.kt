package rs.dusk.network.rs.codec.game.encode.message

import rs.dusk.core.network.model.message.Message

/**
 * Displays a interface onto the client screen
 * @param permanent Whether the interface should be removed on player movement
 * @param parent The id of the parent interface
 * @param component The index of the component
 * @param id The id of the interface to display
 */
data class InterfaceOpenMessage(val permanent: Boolean, val parent: Int, val component: Int, val id: Int) : Message
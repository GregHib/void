package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message

/**
 * Notification that the player has changed their screen mode and might need a gameframe refresh
 * @param displayMode The client display mode
 * @param width The client window width
 * @param height The client window height
 * @param antialiasLevel The client antialias level
 */
data class ScreenChangeMessage(val displayMode: Int, val width: Int, val height: Int, val antialiasLevel: Int) : Message
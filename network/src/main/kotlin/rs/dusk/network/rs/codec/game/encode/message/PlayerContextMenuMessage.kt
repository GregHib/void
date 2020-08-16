package rs.dusk.network.rs.codec.game.encode.message

import rs.dusk.core.network.model.message.Message

/**
 * Sends a player right click option
 * @param option The option
 * @param slot The index of the option
 * @param top Whether it should be forced to the top?
 * @param cursor Unknown value
 */
data class ContextMenuOptionMessage(val option: String?, val slot: Int, val top: Boolean, val cursor: Int = -1) : Message
package rs.dusk.network.rs.codec.game.encode.message

import rs.dusk.core.network.model.message.Message

/**
 * Sends settings to a interface's component(s)
 * @param id The id of the parent window
 * @param component The index of the component
 * @param fromSlot The start slot index
 * @param toSlot The end slot index
 * @param settings The settings hash
 */
data class InterfaceSettingsMessage(val id: Int, val component: Int, val fromSlot: Int, val toSlot: Int, val settings: Int) : Message
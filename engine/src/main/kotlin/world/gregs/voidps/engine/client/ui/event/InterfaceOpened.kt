package world.gregs.voidps.engine.client.ui.event

import world.gregs.voidps.engine.event.Event

/**
 * Notification that an interface was opened.
 * @see [InterfaceRefreshed] for re-opened interfaces
 */
data class InterfaceOpened(val id: Int, val name: String) : Event
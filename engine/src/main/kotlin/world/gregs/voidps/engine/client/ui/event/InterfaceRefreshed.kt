package world.gregs.voidps.engine.client.ui.event

import world.gregs.voidps.engine.event.Event

/**
 * When an interface is initially opened or opened again
 * Primarily for interface changes like unlocking.
 */
data class InterfaceRefreshed(val id: String) : Event
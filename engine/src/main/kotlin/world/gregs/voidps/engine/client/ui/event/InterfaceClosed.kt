package world.gregs.voidps.engine.client.ui.event

import world.gregs.voidps.engine.event.Event

/**
 * An interface was open and has now been closed
 * For close attempts see [CloseInterface]
 */
data class InterfaceClosed(val id: String) : Event
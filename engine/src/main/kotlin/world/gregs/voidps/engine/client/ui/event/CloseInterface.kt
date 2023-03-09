package world.gregs.voidps.engine.client.ui.event

import world.gregs.voidps.engine.event.Event

/**
 * Attempt to close any interface
 * Successfully closed interfaces will also emit [InterfaceClosed]
 */
object CloseInterface : Event
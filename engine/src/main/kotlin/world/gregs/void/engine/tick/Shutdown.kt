package world.gregs.void.engine.tick

import world.gregs.void.engine.event.Event
import world.gregs.void.engine.event.EventCompanion

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
object Shutdown : Event<Unit>(), EventCompanion<Shutdown>
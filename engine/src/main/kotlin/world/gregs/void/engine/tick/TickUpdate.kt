package world.gregs.void.engine.tick

import world.gregs.void.engine.event.Event
import world.gregs.void.engine.event.EventCompanion

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since July 11, 2020
 */
object TickUpdate : Event<Unit>(), EventCompanion<TickUpdate>
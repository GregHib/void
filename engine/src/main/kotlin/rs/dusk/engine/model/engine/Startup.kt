package rs.dusk.engine.model.engine

import rs.dusk.engine.event.Event
import rs.dusk.engine.event.EventCompanion

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
object Startup : Event<Unit>(), EventCompanion<Startup>
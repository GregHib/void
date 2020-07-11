package rs.dusk.engine.model.engine

import rs.dusk.engine.event.Event
import rs.dusk.engine.event.EventCompanion

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since July 11, 2020
 */
object TickInput : Event<Unit>(), EventCompanion<TickInput>
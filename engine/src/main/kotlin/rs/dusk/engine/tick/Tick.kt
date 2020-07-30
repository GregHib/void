package rs.dusk.engine.tick

import rs.dusk.engine.event.Event
import rs.dusk.engine.event.EventCompanion

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
data class Tick(val tick: Long) : Event<Unit>() {
    companion object : EventCompanion<Tick>
}
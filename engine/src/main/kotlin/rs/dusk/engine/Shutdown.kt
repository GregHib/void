package rs.dusk.engine

import rs.dusk.engine.event.Event
import rs.dusk.engine.event.EventCompanion

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
class Shutdown : Event() {
    companion object : EventCompanion<Shutdown>
}
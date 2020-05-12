package rs.dusk.engine.entity.event

import rs.dusk.engine.entity.model.Entity
import rs.dusk.engine.event.Event
import rs.dusk.engine.event.EventCompanion

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
data class Deregistered(val entity: Entity) : Event() {
    companion object : EventCompanion<Deregistered>
}
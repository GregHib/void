package org.redrune.engine.entity.event

import org.redrune.engine.entity.model.Entity
import org.redrune.engine.event.Event
import org.redrune.engine.event.EventCompanion

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
data class Registered(val entity: Entity) : Event() {
    companion object : EventCompanion<Registered>()
}
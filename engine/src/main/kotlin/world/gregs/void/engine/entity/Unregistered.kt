package world.gregs.void.engine.entity

import world.gregs.void.engine.event.Event
import world.gregs.void.engine.event.EventCompanion

/**
 * @author GregHib <greg@gregs.world>
 * @since March 28, 2020
 */
data class Unregistered(val entity: Entity) : Event<Unit>() {
    companion object : EventCompanion<Unregistered>
}
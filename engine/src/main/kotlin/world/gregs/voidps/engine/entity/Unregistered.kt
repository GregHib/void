package world.gregs.voidps.engine.entity

import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventCompanion

/**
 * @author GregHib <greg@gregs.world>
 * @since March 28, 2020
 */
data class Unregistered(val entity: Entity) : Event {
    companion object : EventCompanion<Unregistered>
}
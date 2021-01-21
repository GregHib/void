package world.gregs.void.engine.tick

import world.gregs.void.engine.event.Event
import world.gregs.void.engine.event.EventCompanion

/**
 * @author GregHib <greg@gregs.world>
 * @since March 28, 2020
 */
data class Tick(val tick: Long) : Event<Unit>() {
    companion object : EventCompanion<Tick>
}
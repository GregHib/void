package world.gregs.voidps.engine.tick

import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventCompanion

/**
 * @author GregHib <greg@gregs.world>
 * @since March 28, 2020
 */
data class Tick(val tick: Long) : Event {
    companion object : EventCompanion<Tick>
}
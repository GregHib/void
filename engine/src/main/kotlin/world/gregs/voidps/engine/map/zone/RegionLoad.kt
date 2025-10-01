package world.gregs.voidps.engine.map.zone

import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher

/**
 * Send region load to a player
 */
object RegionLoad : Event {
    override val size = 1

    override fun parameter(dispatcher: EventDispatcher, index: Int) = if (index == 0) "region_load" else null
}

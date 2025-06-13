package world.gregs.voidps.engine.map.zone

import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.type.Region

data class ClearRegion(val region: Region) : Event {
    override val size = 1

    override fun parameter(dispatcher: EventDispatcher, index: Int) = if (index == 0) "clear_region" else null
}

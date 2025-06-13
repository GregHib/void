package world.gregs.voidps.engine.map.zone

import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.type.Zone

data class ReloadZone(val zone: Zone) : Event {
    override val size = 1

    override fun parameter(dispatcher: EventDispatcher, index: Int) = if (index == 0) "reload_zone" else null
}

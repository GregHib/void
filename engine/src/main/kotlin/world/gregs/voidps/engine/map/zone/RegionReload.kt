package world.gregs.voidps.engine.map.zone

import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.type.Region

/**
 * A region has been changed and needs updating for all players
 */
data class RegionReload(val region: Region) : Event {
    override val size = 1

    override fun parameter(dispatcher: EventDispatcher, index: Int) = if (index == 0) "region_reload" else null
}

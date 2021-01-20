package world.gregs.void.engine.map.region

import world.gregs.void.engine.event.Event
import world.gregs.void.engine.event.EventCompanion

data class RegionLoaded(val region: Region) : Event<Unit>() {
    companion object : EventCompanion<RegionLoaded>
}
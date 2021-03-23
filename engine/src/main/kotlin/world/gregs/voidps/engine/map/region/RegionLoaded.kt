package world.gregs.voidps.engine.map.region

import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventCompanion

data class RegionLoaded(val region: Region) : Event {
    companion object : EventCompanion<RegionLoaded>
}
package rs.dusk.engine.map.region

import rs.dusk.engine.event.Event
import rs.dusk.engine.event.EventCompanion

data class RegionLoaded(val region: Region) : Event<Unit>() {
    companion object : EventCompanion<RegionLoaded>
}
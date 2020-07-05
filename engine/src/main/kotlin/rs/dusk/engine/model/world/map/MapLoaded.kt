package rs.dusk.engine.model.world.map

import rs.dusk.engine.event.Event
import rs.dusk.engine.event.EventCompanion
import rs.dusk.engine.model.world.Region

data class MapLoaded(val region: Region) : Event() {
    companion object : EventCompanion<MapLoaded>
}
package rs.dusk.engine.model.world

import rs.dusk.engine.event.Event
import rs.dusk.engine.event.EventCompanion

data class ReloadRegion(val region: Region) : Event<Unit>() {
    companion object : EventCompanion<ReloadRegion>
}
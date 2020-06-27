package rs.dusk.world.entity.obj

import rs.dusk.engine.event.Event
import rs.dusk.engine.event.EventCompanion
import rs.dusk.engine.model.entity.obj.Location

data class ClearObject(
    val location: Location
) : Event() {
    companion object : EventCompanion<ClearObject>
}
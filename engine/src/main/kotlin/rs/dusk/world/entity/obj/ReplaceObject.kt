package rs.dusk.world.entity.obj

import rs.dusk.engine.event.Event
import rs.dusk.engine.event.EventCompanion
import rs.dusk.engine.model.entity.obj.Location
import rs.dusk.engine.model.world.Tile

/**
 * Replaces an existing map objects with [id] [tile] [type] and [rotation] provided.
 * The replacement can be permanent if [ticks] is -1 or temporary
 * [owner] is also optional to allow for an object to replaced just for one player.
 */
data class ReplaceObject(
    val original: Location,
    val id: Int,
    val tile: Tile,
    val type: Int,
    val rotation: Int,
    val ticks: Int,
    val owner: String? = null
) : Event() {
    companion object : EventCompanion<ReplaceObject>
}
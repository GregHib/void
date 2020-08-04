package rs.dusk.world.interact.entity.obj

import rs.dusk.engine.entity.obj.GameObject
import rs.dusk.engine.event.Event
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.EventCompanion
import rs.dusk.engine.map.Tile
import rs.dusk.utility.get

/**
 * Replaces an existing map objects with [id] [tile] [type] and [rotation] provided.
 * The replacement can be permanent if [ticks] is -1 or temporary
 * [owner] is also optional to allow for an object to replaced just for one player.
 */
data class ReplaceObject(
    val original: GameObject,
    val id: Int,
    val tile: Tile,
    val type: Int,
    val rotation: Int,
    val ticks: Int,
    val owner: String? = null
) : Event<Unit>() {
    companion object : EventCompanion<ReplaceObject>
}

fun replaceObject(
    original: GameObject,
    id: Int,
    tile: Tile,
    type: Int = 0,
    rotation: Int = 0,
    ticks: Int = -1,
    owner: String? = null
) = get<EventBus>().emit(ReplaceObject(original, id, tile, type, rotation, ticks, owner))
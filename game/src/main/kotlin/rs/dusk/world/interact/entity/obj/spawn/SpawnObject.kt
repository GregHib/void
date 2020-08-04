package rs.dusk.world.interact.entity.obj.spawn

import rs.dusk.engine.event.Event
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.EventCompanion
import rs.dusk.engine.map.Tile
import rs.dusk.utility.get

/**
 * Spawns a temporary object with [id] [tile] [type] and [rotation] provided.
 * Can be removed after [ticks] or -1 for permanent (until server restarts or removed)
 */
data class SpawnObject(
    val id: Int,
    val tile: Tile,
    val type: Int,
    val rotation: Int,
    val ticks: Int,
    val owner: String? = null
) : Event<Unit>() {
    companion object : EventCompanion<SpawnObject>
}

fun spawnObject(
    id: Int,
    tile: Tile,
    type: Int,
    rotation: Int,
    ticks: Int,
    owner: String? = null
) = get<EventBus>().emit(
    SpawnObject(
        id,
        tile,
        type,
        rotation,
        ticks,
        owner
    )
)
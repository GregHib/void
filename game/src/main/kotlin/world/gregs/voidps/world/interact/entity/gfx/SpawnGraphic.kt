package world.gregs.voidps.world.interact.entity.gfx

import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventBus
import world.gregs.voidps.engine.event.EventCompanion
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.utility.get

data class SpawnGraphic(
    val id: Int,
    val tile: Tile,
    val delay: Int = 0,
    val height: Int = 0,
    val rotation: Int = 0,
    val forceRefresh: Boolean = false,
    val owner: String? = null
) : Event() {
    companion object : EventCompanion<SpawnGraphic>
}

fun areaGraphic(
    id: Int,
    tile: Tile,
    delay: Int = 0,
    height: Int = 0,
    rotation: Int = 0,
    forceRefresh: Boolean = false,
    owner: String? = null
) = get<EventBus>().emit(
    SpawnGraphic(
        id = id,
        tile = tile,
        delay = delay,
        height = height,
        rotation = rotation,
        forceRefresh = forceRefresh,
        owner = owner
    )
)
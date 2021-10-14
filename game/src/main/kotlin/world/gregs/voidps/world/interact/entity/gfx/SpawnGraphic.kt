package world.gregs.voidps.world.interact.entity.gfx

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.definition.GraphicDefinitions
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.utility.get

data class SpawnGraphic(
    val id: Int,
    val tile: Tile,
    val delay: Int = 0,
    val height: Int = 0,
    val rotation: Int = 0,
    val forceRefresh: Boolean = false,
    val owner: String? = null
) : Event

fun areaGraphic(
    name: String,
    tile: Tile,
    delay: Int = 0,
    height: Int = 0,
    rotation: Direction = Direction.SOUTH,
    forceRefresh: Boolean = false,
    owner: String? = null
) {
    areaGraphic(get<GraphicDefinitions>().getIdOrNull(name) ?: return, tile, delay, height, rotation.ordinal, forceRefresh, owner)
}

fun areaGraphic(
    id: Int,
    tile: Tile,
    delay: Int = 0,
    height: Int = 0,
    rotation: Int = 0,
    forceRefresh: Boolean = false,
    owner: String? = null
) = World.events.emit(
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
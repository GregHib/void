package world.gregs.voidps.world.interact.entity.proj

import world.gregs.voidps.cache.definition.data.GraphicDefinition
import world.gregs.voidps.engine.data.definition.extra.GraphicDefinitions
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.world.interact.entity.combat.height
import world.gregs.voidps.world.interact.entity.proj.ShootProjectile.Companion.DEFAULT_CURVE
import world.gregs.voidps.world.interact.entity.proj.ShootProjectile.Companion.DEFAULT_DELAY
import world.gregs.voidps.world.interact.entity.proj.ShootProjectile.Companion.DEFAULT_OFFSET

data class ShootProjectile(
    val id: String,
    val tile: Tile,
    val direction: Delta,
    val target: Character? = null,
    val delay: Int = DEFAULT_DELAY,
    val flightTime: Int = DEFAULT_FLIGHT,
    val startHeight: Int = DEFAULT_HEIGHT,
    val endHeight: Int = startHeight,
    val curve: Int = DEFAULT_CURVE,
    val offset: Int = DEFAULT_OFFSET
) : Event {

    constructor(
        tile: Tile,
        target: Character,
        id: String,
        delay: Int = DEFAULT_DELAY,
        flightTime: Int = DEFAULT_FLIGHT,
        startHeight: Int = DEFAULT_HEIGHT,
        endHeight: Int = startHeight,
        curve: Int = DEFAULT_CURVE,
        offset: Int = DEFAULT_OFFSET,
        sourceSize: Int = 1
    ) : this(
        id = id,
        tile = tile,
        direction = target.tile.delta(tile),
        target = target,
        delay = delay,
        flightTime = flightTime,
        startHeight = startHeight,
        endHeight = endHeight,
        curve = curve,
        offset = (sourceSize * 64) + offset
    )

    companion object {
        const val DEFAULT_FLIGHT = 40
        const val DEFAULT_HEIGHT = 40
        const val DEFAULT_CURVE = 0
        const val DEFAULT_OFFSET = 0
        const val DEFAULT_DELAY = 0
    }
}

fun Tile.shoot(
    id: String,
    target: Character,
    delay: Int? = null,
    flightTime: Int? = null,
    height: Int? = null,
    endHeight: Int? = null,
    curve: Int? = null,
    offset: Int? = null,
) = projectile(id = id,
    target = target,
    flightTime = flightTime,
    delay = delay,
    startHeight = height,
    endHeight = endHeight,
    curve = curve,
    offset = offset,
    targetHeight = target.height,
    targetTile = target.tile,
    sourceTile = this)

fun Tile.shoot(
    id: String,
    tile: Tile,
    delay: Int? = null,
    flightTime: Int? = null,
    height: Int? = null,
    endHeight: Int? = null,
    curve: Int? = null,
    offset: Int? = null
) = projectile(id = id,
    flightTime = flightTime,
    delay = delay,
    startHeight = height,
    endHeight = endHeight,
    curve = curve,
    offset = offset,
    targetTile = tile,
    sourceTile = this)

fun Character.shoot(
    id: String,
    target: Character,
    delay: Int? = null,
    flightTime: Int? = null,
    height: Int? = null,
    endHeight: Int? = null,
    curve: Int? = null,
    offset: Int? = null,
) = projectile(id = id,
    target = target,
    flightTime = flightTime,
    delay = delay,
    startHeight = height,
    endHeight = endHeight,
    curve = curve,
    offset = offset,
    width = size.width,
    sourceHeight = this.height,
    targetHeight = target.height,
    targetTile = target.tile,
    sourceTile = tile)

fun Character.shoot(
    id: String,
    tile: Tile,
    delay: Int? = null,
    flightTime: Int? = null,
    height: Int? = null,
    endHeight: Int? = null,
    curve: Int? = null,
    offset: Int? = null
) = projectile(id = id,
    targetTile = tile,
    delay = delay,
    flightTime = flightTime,
    startHeight = height,
    endHeight = endHeight,
    curve = curve,
    offset = offset,
    width = size.width,
    sourceHeight = this.height,
    sourceTile = this.tile)

private fun projectile(
    id: String,
    sourceTile: Tile,
    delay: Int?,
    flightTime: Int?,
    startHeight: Int?,
    endHeight: Int?,
    curve: Int?,
    offset: Int?,
    targetTile: Tile,
    target: Character? = null,
    width: Int = 1,
    sourceHeight: Int = 0,
    targetHeight: Int = 0
) {
    val definition = get<GraphicDefinitions>().getOrNull(id) ?: return
    val time = getFlightTime(definition, sourceTile, targetTile, flightTime)
    if (time == -1) {
        return
    }
    World.events.emit(
        ShootProjectile(
            id = id,
            tile = sourceTile,
            direction = targetTile.delta(sourceTile),
            target = target,
            delay = delay ?: definition["delay", DEFAULT_DELAY],
            flightTime = time,
            startHeight = startHeight ?: (sourceHeight + definition["height", 0]),
            endHeight = endHeight ?: (targetHeight + definition["end_height", 0]),
            curve = curve ?: definition["curve", DEFAULT_CURVE],
            offset = (width * 64) + (offset ?: definition["offset", DEFAULT_OFFSET])
        )
    )
}

private fun getFlightTime(definition: GraphicDefinition, tile: Tile, target: Tile, flightTime: Int?): Int {
    if (flightTime != null) {
        return flightTime
    }
    return definition.getOrNull<List<Int>>("flight_time")?.getOrNull(tile.distanceTo(target) - 1) ?: -1
}
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
) {
    val definition = get<GraphicDefinitions>().getOrNull(id) ?: return
    val time = getFlightTime(definition, this, target.tile, flightTime)
    if (time == -1) {
        return
    }
    World.events.emit(
        ShootProjectile(
            id = id,
            tile = this,
            direction = target.tile.delta(this),
            target = target,
            delay = delay ?: definition["delay", DEFAULT_DELAY],
            flightTime = time,
            startHeight = height ?: definition["height", 0],
            endHeight = endHeight ?: (target.height + definition["end_height", 0]),
            curve = curve ?: definition["curve", DEFAULT_CURVE],
            offset = 64 + (offset ?: definition["offset", DEFAULT_OFFSET])
        )
    )
}

fun Tile.shoot(
    id: String,
    tile: Tile,
    delay: Int? = null,
    flightTime: Int? = null,
    height: Int? = null,
    endHeight: Int? = null,
    curve: Int? = null,
    offset: Int? = null
) {
    val definition = get<GraphicDefinitions>().getOrNull(id) ?: return
    val time = getFlightTime(definition, this, tile, flightTime)
    if (time == -1) {
        return
    }
    World.events.emit(
        ShootProjectile(
            id = id,
            tile = this,
            direction = tile.delta(this),
            delay = delay ?: definition["delay", DEFAULT_DELAY],
            flightTime = time,
            startHeight = height ?: definition["height", 0],
            endHeight = endHeight ?: definition["end_height", 0],
            curve = curve ?: definition["curve", DEFAULT_CURVE],
            offset = 64 + (offset ?: definition["offset", DEFAULT_OFFSET])
        )
    )
}

fun Character.shoot(
    id: String,
    target: Character,
    delay: Int? = null,
    flightTime: Int? = null,
    height: Int? = null,
    endHeight: Int? = null,
    curve: Int? = null,
    offset: Int? = null,
) {
    val definition = get<GraphicDefinitions>().getOrNull(id) ?: return
    val time = getFlightTime(definition, tile, target.tile, flightTime)
    if (time == -1) {
        return
    }
    World.events.emit(
        ShootProjectile(
            id = id,
            tile = tile,
            direction = target.tile.delta(tile),
            target = target,
            delay = delay ?: definition["delay", DEFAULT_DELAY],
            flightTime = time,
            startHeight = height ?: (this.height + definition["height", 0]),
            endHeight = endHeight ?: (target.height + definition["end_height", 0]),
            curve = curve ?: definition["curve", DEFAULT_CURVE],
            offset = (size.width * 64) + (offset ?: definition["offset", DEFAULT_OFFSET])
        )
    )
}

fun Character.shoot(
    id: String,
    tile: Tile,
    delay: Int? = null,
    flightTime: Int? = null,
    height: Int? = null,
    endHeight: Int? = null,
    curve: Int? = null,
    offset: Int? = null
) {
    val definition = get<GraphicDefinitions>().getOrNull(id) ?: return
    val time = getFlightTime(definition, this.tile, tile, flightTime)
    if (time == -1) {
        return
    }
    World.events.emit(
        ShootProjectile(
            id = id,
            tile = this.tile,
            direction = tile.delta(this.tile),
            delay = delay ?: definition["delay", DEFAULT_DELAY],
            flightTime = time,
            startHeight = height ?: (this.height + definition["height", 0]),
            endHeight = endHeight ?: definition["end_height", 0],
            curve = curve ?: definition["curve", DEFAULT_CURVE],
            offset = (size.width * 64) + (offset ?: definition["offset", DEFAULT_OFFSET])
        )
    )
}

private fun getFlightTime(definition: GraphicDefinition, tile: Tile, target: Tile, flightTime: Int?): Int {
    if (flightTime != null) {
        return flightTime
    }
    return definition.getOrNull<List<Int>>("flight_time")?.getOrNull(tile.distanceTo(target) - 1) ?: -1
}
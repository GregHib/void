package world.gregs.voidps.world.interact.entity.proj

import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.definition.GraphicDefinitions
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.utility.get
import world.gregs.voidps.world.interact.entity.combat.height

data class ShootProjectile(
    val id: Int,
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
        id: Int,
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
        offset = sourceSize * 64 + offset
    )

    companion object {
        const val DEFAULT_FLIGHT = 40
        const val DEFAULT_HEIGHT = 40
        const val DEFAULT_CURVE = 0
        const val DEFAULT_OFFSET = 0
        const val DEFAULT_DELAY = 0
    }
}

fun Character.shoot(
    name: String,
    target: Character,
    delay: Int? = null,
    flightTime: Int? = null,
    height: Int? = null,
    endHeight: Int? = null,
    curve: Int? = null,
    offset: Int? = null,
) {
    val definition = get<GraphicDefinitions>().getOrNull(name) ?: return
    World.events.emit(
        ShootProjectile(
            id = definition.id,
            tile = tile,
            direction = target.tile.delta(tile),
            target = target,
            delay = delay ?: definition["delay", 0],
            flightTime = flightTime ?: definition["flight_time", 0],
            startHeight = height ?: (this.height + definition["height", 0]),
            endHeight = endHeight ?: (target.height + definition["end_height", 0]),
            curve = curve ?: definition["curve", 0],
            offset = size.width * 64 + (offset ?: definition["offset", 0])
        )
    )
}

fun Character.shoot(
    name: String,
    direction: Tile,
    delay: Int? = null,
    flightTime: Int? = null,
    height: Int? = null,
    endHeight: Int? = null,
    curve: Int? = null,
    offset: Int? = null
) {
    val definition = get<GraphicDefinitions>().getOrNull(name) ?: return
    shoot(
        id = definition.id,
        direction = direction,
        delay = delay ?: definition["delay", 0],
        flightTime = flightTime ?: definition["flight_time", 0],
        height = height ?: (this.height + definition["height", 40]),
        endHeight = endHeight ?: definition["end_height", 40],
        curve = curve ?: definition["curve", 0],
        offset = offset ?: definition["offset", 0]
    )
}

fun Character.shoot(
    id: Int,
    direction: Tile,
    delay: Int = ShootProjectile.DEFAULT_DELAY,
    flightTime: Int = ShootProjectile.DEFAULT_FLIGHT,
    height: Int = ShootProjectile.DEFAULT_HEIGHT,
    endHeight: Int = height,
    curve: Int = ShootProjectile.DEFAULT_CURVE,
    offset: Int = ShootProjectile.DEFAULT_OFFSET
) {
    World.events.emit(
        ShootProjectile(
            id = id,
            tile = tile,
            direction = direction.delta(tile),
            delay = delay,
            flightTime = flightTime,
            startHeight = height,
            endHeight = endHeight,
            curve = curve,
            offset = size.width * 64 + offset
        )
    )
}
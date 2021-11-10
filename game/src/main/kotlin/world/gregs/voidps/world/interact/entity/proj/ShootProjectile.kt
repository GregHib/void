package world.gregs.voidps.world.interact.entity.proj

import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.definition.GraphicDefinitions
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.world.interact.entity.combat.height
import world.gregs.voidps.world.interact.entity.proj.ShootProjectile.Companion.DEFAULT_CURVE
import world.gregs.voidps.world.interact.entity.proj.ShootProjectile.Companion.DEFAULT_DELAY
import world.gregs.voidps.world.interact.entity.proj.ShootProjectile.Companion.DEFAULT_FLIGHT
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
    World.events.emit(
        ShootProjectile(
            id = id,
            tile = tile,
            direction = target.tile.delta(tile),
            target = target,
            delay = delay ?: definition["delay", DEFAULT_DELAY],
            flightTime = flightTime ?: definition["flight_time", DEFAULT_FLIGHT],
            startHeight = height ?: (this.height + definition["height", 0]),
            endHeight = endHeight ?: (target.height + definition["end_height", 0]),
            curve = curve ?: definition["curve", DEFAULT_CURVE],
            offset = size.width * 64 + (offset ?: definition["offset", DEFAULT_OFFSET])
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
    World.events.emit(
        ShootProjectile(
            id = id,
            tile = this.tile,
            direction = tile.delta(this.tile),
            delay = delay ?: definition["delay", DEFAULT_DELAY],
            flightTime = flightTime ?: definition["flight_time", DEFAULT_FLIGHT],
            startHeight = height ?: (this.height + definition["height", 0]),
            endHeight = endHeight ?: definition["end_height", 0],
            curve = curve ?: definition["curve", DEFAULT_CURVE],
            offset = offset ?: definition["offset", DEFAULT_OFFSET]
        )
    )
}
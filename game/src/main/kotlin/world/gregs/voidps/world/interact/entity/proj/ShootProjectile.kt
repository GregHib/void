package world.gregs.voidps.world.interact.entity.proj

import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.definition.GraphicDefinitions
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.utility.get

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
    delay: Int = ShootProjectile.DEFAULT_DELAY,
    flightTime: Int = ShootProjectile.DEFAULT_FLIGHT,
    height: Int = ShootProjectile.DEFAULT_HEIGHT,
    endHeight: Int = height,
    curve: Int = ShootProjectile.DEFAULT_CURVE,
    offset: Int = ShootProjectile.DEFAULT_OFFSET
) {
    val id = get<GraphicDefinitions>().getIdOrNull(name) ?: return
    World.events.emit(
        ShootProjectile(
            id = id,
            tile = tile,
            direction = target.tile.delta(tile),
            target = target,
            delay = delay,
            flightTime = flightTime,
            startHeight = height,
            endHeight = endHeight,
            curve = curve,
            offset = size.width * 64 + offset
        )
    )
}

fun Character.shoot(
    name: String,
    direction: Tile,
    delay: Int = ShootProjectile.DEFAULT_DELAY,
    flightTime: Int = ShootProjectile.DEFAULT_FLIGHT,
    height: Int = ShootProjectile.DEFAULT_HEIGHT,
    endHeight: Int = height,
    curve: Int = ShootProjectile.DEFAULT_CURVE,
    offset: Int = ShootProjectile.DEFAULT_OFFSET
) {
    shoot(
        get<GraphicDefinitions>().getIdOrNull(name) ?: return,
        direction,
        delay,
        flightTime,
        height,
        endHeight,
        curve,
        offset
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
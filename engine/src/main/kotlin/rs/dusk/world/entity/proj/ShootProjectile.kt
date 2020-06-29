package rs.dusk.world.entity.proj

import rs.dusk.engine.event.Event
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.EventCompanion
import rs.dusk.engine.model.entity.index.Character
import rs.dusk.engine.model.world.Tile
import rs.dusk.utility.get

data class ShootProjectile(
    val id: Int,
    val tile: Tile,
    val direction: Tile,
    val target: Character? = null,
    val delay: Int = DEFAULT_DELAY,
    val flightTime: Int = DEFAULT_FLIGHT,
    val startHeight: Int = DEFAULT_HEIGHT,
    val endHeight: Int = startHeight,
    val curve: Int = DEFAULT_CURVE,
    val offset: Int = DEFAULT_OFFSET
) : Event() {

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
        id,
        tile,
        target.tile.delta(tile),
        target,
        delay,
        flightTime,
        startHeight,
        endHeight,
        curve,
        sourceSize * 64 + offset
    )

    companion object : EventCompanion<ShootProjectile> {
        const val DEFAULT_FLIGHT = 40
        const val DEFAULT_HEIGHT = 40
        const val DEFAULT_CURVE = 0
        const val DEFAULT_OFFSET = 0
        const val DEFAULT_DELAY = 0
    }
}

fun Character.shoot(
    id: Int,
    target: Character,
    delay: Int = ShootProjectile.DEFAULT_DELAY,
    flightTime: Int = ShootProjectile.DEFAULT_FLIGHT,
    height: Int = ShootProjectile.DEFAULT_HEIGHT,
    endHeight: Int = height,
    curve: Int = ShootProjectile.DEFAULT_CURVE,
    offset: Int = ShootProjectile.DEFAULT_OFFSET
) {
    val bus: EventBus = get()
    bus.emit(
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
    id: Int,
    direction: Tile,
    delay: Int = ShootProjectile.DEFAULT_DELAY,
    flightTime: Int = ShootProjectile.DEFAULT_FLIGHT,
    height: Int = ShootProjectile.DEFAULT_HEIGHT,
    endHeight: Int = height,
    curve: Int = ShootProjectile.DEFAULT_CURVE,
    offset: Int = ShootProjectile.DEFAULT_OFFSET
) {
    val bus: EventBus = get()
    bus.emit(
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
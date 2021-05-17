package world.gregs.voidps.world.interact.entity.sound

import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.definition.SoundDefinitions
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.utility.get

data class PlaySound(
    val id: Int,
    val tile: Tile,
    val radius: Int = 1,
    val repeat: Int = 1,
    val delay: Int = 0,
    val volume: Int = 255,
    val speed: Int = 255,
    val midi: Boolean = false,
    val owner: String? = null
) : Event {
    init {
        assert(radius > 0) { "Radius must be greater than zero" }
    }
}

fun areaMidi(
    name: String,
    tile: Tile,
    radius: Int,
    repeat: Int = 1,
    delay: Int = 0,
    volume: Int = 255,
    speed: Int = 255,
    owner: String? = null
){
    areaMidi(get<SoundDefinitions>().getIdOrNull(name) ?: return, tile, radius, repeat, delay, volume, speed, owner)
}

fun areaMidi(
    id: Int,
    tile: Tile,
    radius: Int,
    repeat: Int = 1,
    delay: Int = 0,
    volume: Int = 255,
    speed: Int = 255,
    owner: String? = null
) = World.events.emit(
    PlaySound(
        id = id,
        tile = tile,
        radius = radius,
        repeat = repeat,
        delay = delay,
        volume = volume,
        speed = speed,
        midi = true,
        owner = owner
    )
)

fun areaSound(
    name: String,
    tile: Tile,
    radius: Int,
    repeat: Int = 1,
    delay: Int = 0,
    volume: Int = 255,
    speed: Int = 255,
    owner: String? = null
) {
    areaSound(get<SoundDefinitions>().getIdOrNull(name) ?: return, tile, radius, repeat, delay, volume, speed, owner)
}

fun areaSound(
    id: Int,
    tile: Tile,
    radius: Int,
    repeat: Int = 1,
    delay: Int = 0,
    volume: Int = 255,
    speed: Int = 255,
    owner: String? = null
) = World.events.emit(
    PlaySound(
        id = id,
        tile = tile,
        radius = radius,
        repeat = repeat,
        delay = delay,
        volume = volume,
        speed = speed,
        midi = false,
        owner = owner
    )
)
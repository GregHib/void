package content.entity.proj

import content.entity.proj.ShootProjectile.DEFAULT_CURVE
import content.entity.proj.ShootProjectile.DEFAULT_DELAY
import content.entity.proj.ShootProjectile.DEFAULT_OFFSET
import world.gregs.voidps.cache.definition.data.GraphicDefinition
import world.gregs.voidps.engine.client.update.batch.ZoneBatchUpdates
import world.gregs.voidps.engine.data.definition.GraphicDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.get
import world.gregs.voidps.network.login.protocol.encode.zone.ProjectileAddition
import world.gregs.voidps.type.Delta
import world.gregs.voidps.type.Tile

object ShootProjectile {
    const val DEFAULT_FLIGHT = 40
    const val DEFAULT_HEIGHT = 40
    const val DEFAULT_CURVE = 0
    const val DEFAULT_OFFSET = 0
    const val DEFAULT_DELAY = 0
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
    width: Int = 1,
) = projectile(
    id = id,
    target = target,
    flightTime = flightTime,
    delay = delay,
    startHeight = height,
    endHeight = endHeight,
    curve = curve,
    offset = offset,
    targetHeight = target.height,
    targetTile = target.tile,
    sourceTile = this,
    width = width,
)

fun Tile.shoot(
    id: String,
    tile: Tile,
    delay: Int? = null,
    flightTime: Int? = null,
    height: Int? = null,
    endHeight: Int? = null,
    curve: Int? = null,
    offset: Int? = null,
) = projectile(
    id = id,
    flightTime = flightTime,
    delay = delay,
    startHeight = height,
    endHeight = endHeight,
    curve = curve,
    offset = offset,
    targetTile = tile,
    sourceTile = this,
)

fun Character.shoot(
    id: String,
    target: Character,
    delay: Int? = null,
    flightTime: Int? = null,
    height: Int? = null,
    endHeight: Int? = null,
    curve: Int? = null,
    offset: Int? = null,
    width: Int = size,
    tileOffset: Int = 0
) = projectile(
    id = id,
    target = target,
    flightTime = flightTime,
    delay = delay,
    startHeight = height,
    endHeight = endHeight,
    curve = curve,
    offset = offset,
    width = width,
    sourceHeight = this.height,
    targetHeight = target.height,
    targetTile = target.tile,
    sourceTile = tile.add(tileOffset, tileOffset),
)

fun Character.shoot(
    id: String,
    tile: Tile,
    delay: Int? = null,
    flightTime: Int? = null,
    height: Int? = null,
    endHeight: Int? = null,
    curve: Int? = null,
    offset: Int? = null,
) = projectile(
    id = id,
    targetTile = tile,
    delay = delay,
    flightTime = flightTime,
    startHeight = height,
    endHeight = endHeight,
    curve = curve,
    offset = offset,
    width = size,
    sourceHeight = this.height,
    sourceTile = this.tile,
)

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
    targetHeight: Int = 0,
): Int {
    val definition = get<GraphicDefinitions>().getOrNull(id) ?: return -1
    val time = flightTime(definition, sourceTile, targetTile, flightTime)
    if (time == -1) {
        return -1
    }
    val startDelay = delay ?: definition["delay", DEFAULT_DELAY]
    sendProjectile(
        id = id,
        tile = sourceTile,
        direction = targetTile.delta(sourceTile),
        target = target,
        delay = startDelay,
        flightTime = time,
        startHeight = startHeight ?: (sourceHeight + definition["height", 0]),
        endHeight = endHeight ?: (targetHeight + definition["end_height", 0]),
        curve = curve ?: definition["curve", DEFAULT_CURVE],
        offset = (width * 64) + (offset ?: definition["size_offset", DEFAULT_OFFSET]),
    )
    return time + startDelay
}

private fun sendProjectile(
    id: String,
    tile: Tile,
    direction: Delta,
    target: Character? = null,
    delay: Int = DEFAULT_DELAY,
    flightTime: Int = ShootProjectile.DEFAULT_FLIGHT,
    startHeight: Int = ShootProjectile.DEFAULT_HEIGHT,
    endHeight: Int = startHeight,
    curve: Int = DEFAULT_CURVE,
    offset: Int = DEFAULT_OFFSET,
) {
    val batches: ZoneBatchUpdates = get()
    val definitions: GraphicDefinitions = get()
    var index = if (target != null) target.index + 1 else 0
    if (target is Player) {
        index = -index
    }
    batches.add(
        tile.zone,
        ProjectileAddition(
            tile = tile.id,
            id = definitions.get(id).id,
            index = index,
            directionX = direction.x,
            directionY = direction.y,
            startHeight = startHeight,
            endHeight = endHeight,
            delay = delay,
            flightTime = flightTime,
            curve = curve,
            offset = offset,
        ),
    )
}

private fun flightTime(definition: GraphicDefinition, tile: Tile, target: Tile, flightTime: Int?): Int {
    if (flightTime != null) {
        return flightTime
    }
    val offset = definition.getOrNull("time_offset") ?: 0
    val multiplier = definition.getOrNull("multiplier") ?: 5
    val distance = tile.distanceTo(target) - 1
    return (offset + (distance * multiplier)).coerceAtLeast(-1)
}

private val Character.height: Int
    get() = (this as? NPC)?.def?.getOrNull("height") ?: ShootProjectile.DEFAULT_HEIGHT

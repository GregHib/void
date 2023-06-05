package world.gregs.voidps.world.map

import world.gregs.voidps.engine.client.update.batch.animate
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.mode.move.Moved
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectGroup
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.Distance.nearestTo
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.Areas
import world.gregs.voidps.engine.map.area.Cuboid
import world.gregs.voidps.engine.map.area.Rectangle
import world.gregs.voidps.engine.map.chunk.Chunk
import kotlin.collections.set

val objects: GameObjects by inject()
val areas: Areas by inject()

val borders = mutableMapOf<Chunk, Rectangle>()

on<World, Registered> {
    for (border in areas.getTagged("border")) {
        val passage = border.area as Cuboid
        for (chunk in passage.toChunks()) {
            borders[chunk] = passage.toRectangles().first()
        }
    }
}

on<Moved>({ enteringBorder(from, to) }) { player: Player ->
    val border = borders[to.chunk] ?: return@on
    val tile = border.nearestTo(player.tile)
    player.start("no_clip", 3)
    player.start("slow_run", 3)
    if (player.steps.destination in border) {
        val endSide = getOppositeSide(border, tile)
        player.walkTo(endSide)
    }
    val guards = getGuards(tile)
    changeGuardState(guards, true)
}

on<Moved>({ exitingBorder(from, to) }) { player: Player ->
    val border = borders[to.chunk] ?: return@on
    val tile = border.nearestTo(player.tile)
    player.stop("no_clip")
    player.stop("slow_run")
    val guards = getGuards(tile)
    changeGuardState(guards, false)
}

val raised = mutableMapOf<GameObject, Boolean>()

fun getGuards(tile: Tile) =
    tile.chunk.toCuboid()
        .mapNotNull { objects.getGroup(tile, ObjectGroup.INTERACTIVE) }
        .union(tile.chunk.add(0, 1).toCuboid().mapNotNull { objects.getGroup(tile, ObjectGroup.INTERACTIVE) })
        .filter { it.id.startsWith("border_guard") }

fun changeGuardState(guards: List<GameObject>, raise: Boolean) {
    for (guard in guards) {
        if (raised.getOrDefault(guard, false) != raise) {
            guard.animate(guard.def[if (raise) "raise" else "lower"])
            raised[guard] = raise
        }
    }
}

fun enteringBorder(from: Tile, to: Tile): Boolean {
    val border = borders[to.chunk] ?: return false
    return !border.contains(from) && border.contains(to)
}

fun exitingBorder(from: Tile, to: Tile): Boolean {
    val border = borders[to.chunk] ?: return false
    return border.contains(from) && !border.contains(to)
}

// Longest axis determines direction, current location above is underside else above
fun getOppositeSide(border: Rectangle, tile: Tile) = if (border.height > border.width) {
    tile.copy(y = if (tile.y > border.minY) border.minY - 1 else border.maxY + 1)
} else {
    tile.copy(x = if (tile.x > border.minX) border.minX - 1 else border.maxX + 1)
}
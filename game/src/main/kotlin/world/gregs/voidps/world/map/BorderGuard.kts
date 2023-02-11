package world.gregs.voidps.world.map

import world.gregs.voidps.engine.client.update.batch.animate
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.mode.move.Moved
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.Distance.nearestTo
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.Areas
import world.gregs.voidps.engine.map.area.Cuboid
import world.gregs.voidps.engine.map.area.Rectangle
import world.gregs.voidps.engine.map.chunk.Chunk

val objects: Objects by inject()
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
    val guards = objects[tile.chunk].filter { it.id.startsWith("border_guard") }
    player.visuals.running = false
    changeGuardState(guards, true)
    player.start("no_clip")
}

on<Moved>({ exitingBorder(from, to) }) { player: Player ->
    val border = borders[to.chunk] ?: return@on
    val tile = border.nearestTo(player.tile)
    val guards = objects[tile.chunk].filter { it.id.startsWith("border_guard") }
    player.visuals.running = player.running
    changeGuardState(guards, false)
    player.stop("no_clip")
}

fun changeGuardState(guards: List<GameObject>, raise: Boolean) {
    for (guard in guards) {
        if (guard["raised", false] != raise) {
            guard.animate(guard.def[if (raise) "raise" else "lower"])
            guard["raised"] = raise
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
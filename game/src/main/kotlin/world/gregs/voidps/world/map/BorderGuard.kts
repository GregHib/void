package world.gregs.voidps.world.map

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.Moved
import world.gregs.voidps.engine.entity.character.Moving
import world.gregs.voidps.engine.entity.character.move.Path
import world.gregs.voidps.engine.entity.character.move.awaitWalk
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.Distance.nearestTo
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.Areas
import world.gregs.voidps.engine.map.area.Cuboid
import world.gregs.voidps.engine.map.area.Rectangle
import world.gregs.voidps.engine.map.chunk.Chunk
import world.gregs.voidps.engine.map.chunk.animate
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.path.strat.TileTargetStrategy
import world.gregs.voidps.engine.tick.Startup
import world.gregs.voidps.engine.utility.inject

val objects: Objects by inject()
val areas: Areas by inject()

data class Border(val area: Cuboid, val passage: Rectangle, val direction: Direction)

val borders = mutableMapOf<Chunk, Border>()

on<World, Startup> {
    for (border in areas.getTagged("border")) {
        val direction = Direction.valueOf(border["direction"])
        val area = border.area as Cuboid
        val passage = if (direction.isVertical()) {
            Rectangle(area.minX + 1, area.minY, area.maxX - 1, area.maxY)
        } else {
            Rectangle(area.minX, area.minY + 1, area.maxX, area.maxY - 1)
        }
        for (chunk in area.toChunks()) {
            borders[chunk] = Border(area, passage, direction)
        }
    }
}

on<Moving>({ enteringBorder(it, to) }) { player: Player ->
    val path = player.movement.path
    if (path.result is PathResult.Partial || path.state == Path.State.Progressing) {
        player["border_old_strategy"] = path.strategy
    } else {
        player.clear("border_old_strategy")
    }
}

on<Moved>({ enteringBorder(it, to) }) { player: Player ->
    val border = borders[to.chunk] ?: return@on
    // Already in area
    if (border.area.contains(from)) {
        return@on
    }
    // Clear existing movement to stay in sync
    player.movement.clear()
    player.action(ActionType.OpenDoor) {
        val tile = border.passage.nearestTo(player.tile)
        withContext(NonCancellable) {
            val run = player.running
            try {
                val guards = objects[tile.chunk].filter { it.id.startsWith("border_guard") }
                player.awaitWalk(tile, ignore = false)
                player.running = false
                changeGuardState(guards, true)
                player.start("no_clip")
                val small = if (border.direction.isVertical()) tile.y <= border.area.minY else tile.x <= border.area.minX
                val multiplier = if (small) 2 else -2
                player.awaitWalk(tile.add(border.direction.delta.x * multiplier, border.direction.delta.y * multiplier), ignore = false) {
                    changeGuardState(guards, false)
                }
            } finally {
                player.stop("no_clip")
                player.running = run
            }
        }
        val strategy = player.remove<TileTargetStrategy>("border_old_strategy") ?: return@action
        player.walkTo(strategy)
    }
}

fun changeGuardState(guards: List<GameObject>, raise: Boolean) {
    for (guard in guards) {
        if (guard["raised", false] != raise) {
            guard.animate(guard.def[if (raise) "raise" else "lower"])
            guard["raised"] = raise
        }
    }
}

fun enteringBorder(player: Player, to: Tile): Boolean {
    return player.action.type != ActionType.OpenDoor && borders.containsKey(to.chunk) && borders[to.chunk]?.area?.contains(to) == true
}
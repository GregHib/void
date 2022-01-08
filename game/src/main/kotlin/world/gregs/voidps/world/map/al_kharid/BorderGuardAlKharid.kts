package world.gregs.voidps.world.map.al_kharid

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.Scheduler
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.entity.character.Moved
import world.gregs.voidps.engine.entity.character.move.Path
import world.gregs.voidps.engine.entity.character.move.awaitWalk
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.entity.stop
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.Distance.nearestTo
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.Rectangle
import world.gregs.voidps.engine.map.chunk.animate
import world.gregs.voidps.engine.utility.inject

val objects: Objects by inject()
val scheduler: Scheduler by inject()
val top = Rectangle(3281, 3330, 3286, 3331)
val bottom = Rectangle(3282, 3329, 3285, 3329)
val center = Rectangle(3283, 3329, 3284, 3331)

on<Moved>({ !top.contains(from) && !bottom.contains(from) && (bottom.contains(to) || top.contains(to)) }) { player: Player ->
    scheduler.sync {
        player.action(ActionType.OpenDoor) {
            val right = objects[Tile(3282, 3330), 45857]!!
            val left = objects[Tile(3284, 3330), 45857]!!
            val tile = center.nearestTo(player.tile)
            val path = player.movement.path
            withContext(NonCancellable) {
                val run = player.running
                try {
                    player.awaitWalk(tile, cancelAction = false, ignore = false)
                    player.running = false
                    right.animate("border_guard_raise")
                    left.animate("border_guard_raise")
                    player.start("no_clip")
                    val north = tile.y <= bottom.minY
                    player.awaitWalk(tile.addY(if (north) 2 else -2), cancelAction = false, ignore = false) {
                        right.animate("border_guard_lower")
                        left.animate("border_guard_lower")
                    }
                } finally {
                    player.stop("no_clip")
                    player.running = run
                }
            }
            if (path.state == Path.State.Progressing) {
                player.walkTo(path.strategy, block = path.callback)
            }
        }
    }
}
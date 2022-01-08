package world.gregs.voidps.engine.entity.character.move

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.Moved
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.cantReach
import world.gregs.voidps.engine.entity.character.player.noInterest
import world.gregs.voidps.engine.entity.character.update.visual.player.face
import world.gregs.voidps.engine.entity.character.update.visual.watch
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.map.Distance.getNearest
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.path.PathFinder
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.path.strat.TileTargetStrategy
import kotlin.coroutines.resume

fun Character.walkTo(target: Any, watch: Character? = null, distance: Int = 0, cancelAction: Boolean = true, ignore: Boolean = true, action: ((Path) -> Unit)? = null) {
    walkTo(PathFinder.getStrategy(target), watch, distance, cancelAction, ignore, action)
}

fun Character.walkTo(strategy: TileTargetStrategy, watch: Character? = null, distance: Int = 0, cancelAction: Boolean = true, ignore: Boolean = true, block: ((Path) -> Unit)? = null) {
    delay(this) {
        awaitWalk(strategy, watch, distance, cancelAction, ignore, block)
    }
}

suspend fun Character.awaitWalk(target: Any, watch: Character? = null, distance: Int = 0, cancelAction: Boolean = true, ignore: Boolean = true, block: ((Path) -> Unit)? = null) {
    awaitWalk(PathFinder.getStrategy(target), watch, distance, cancelAction, ignore, block)
}

suspend fun Character.awaitWalk(strategy: TileTargetStrategy, watch: Character? = null, distance: Int = 0, cancelAction: Boolean = true, ignore: Boolean = true, block: ((Path) -> Unit)? = null) {
    if (cancelAction) {
        action.cancelAndJoin()
    }
    if (strategy.reached(tile, size) || withinDistance(tile, strategy, distance)) {
        block?.invoke(Path.EMPTY)
        return
    }
    var continuation: CancellableContinuation<Path>? = null
    val handler = events.on<Character, Moved>({ withinDistance(to, strategy, distance) }) {
        continuation?.resume(Path.EMPTY)
        continuation = null
    }
    try {
        if (this is Player) {
            dialogues.clear()
            watch(null)
        }
        if (watch != null) {
            watch(watch)
        }
        movement.set(strategy, this is Player, ignore) { path ->
            continuation?.resume(path)
            continuation = null
        }
        // Suspend manually to not interfere with actions.
        val path = suspendCancellableCoroutine<Path> {
            continuation = it
        }
        if (cantReach(path, distance)) {
            (this as? Player)?.cantReach()
        } else {
            block?.invoke(path)
        }
    } finally {
        if (watch != null) {
            watch(null)
            face(watch)
        }
        events.remove(handler)
    }
}

private fun Character.cantReach(path: Path, distance: Int): Boolean {
    return path.result is PathResult.Failure || (path.result is PathResult.Partial && !path.strategy.reached(tile, size) && !withinDistance(tile, path.strategy, distance))
}

private fun withinDistance(tile: Tile, target: TileTargetStrategy, distance: Int): Boolean {
    return distance > 0 && tile.distanceTo(target.tile, target.size) <= distance && tile.withinSight(getNearest(target.tile, target.size, tile), ignore = true)
}

fun Player.interact(event: Event) {
    if (!events.emit(event)) {
        noInterest()
    }
}
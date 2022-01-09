package world.gregs.voidps.engine.entity.character.move

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.entity.character.CantReach
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.MoveStop
import world.gregs.voidps.engine.entity.character.Moved
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.noInterest
import world.gregs.voidps.engine.entity.character.update.visual.player.face
import world.gregs.voidps.engine.entity.character.update.visual.watch
import world.gregs.voidps.engine.entity.remove
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.map.Distance.getNearest
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.path.PathFinder
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.path.PathType
import world.gregs.voidps.engine.path.strat.TileTargetStrategy
import kotlin.coroutines.resume

fun Character.walkTo(
    target: Any,
    watch: Character? = null,
    distance: Int = 0,
    cancelAction: Boolean = true,
    ignore: Boolean = true,
    type: PathType = if (this is Player) PathType.Smart else PathType.Dumb,
    action: ((Path) -> Unit)? = null
) {
    walkTo(PathFinder.getStrategy(target), watch, distance, cancelAction, ignore, type, action)
}

fun Character.walkTo(
    strategy: TileTargetStrategy,
    watch: Character? = null,
    distance: Int = 0,
    cancelAction: Boolean = true,
    ignore: Boolean = true,
    type: PathType = if (this is Player) PathType.Smart else PathType.Dumb,
    block: ((Path) -> Unit)? = null
) {
    delay(this) {
        awaitWalk(strategy, watch, distance, cancelAction, ignore, type, block)
    }
}

suspend fun Character.awaitWalk(
    target: Any,
    watch: Character? = null,
    distance: Int = 0,
    cancelAction: Boolean = true,
    ignore: Boolean = true,
    type: PathType = if (this is Player) PathType.Smart else PathType.Dumb,
    block: ((Path) -> Unit)? = null
) {
    awaitWalk(PathFinder.getStrategy(target), watch, distance, cancelAction, ignore, type, block)
}

suspend fun Character.awaitWalk(
    strategy: TileTargetStrategy,
    watch: Character? = null,
    distance: Int = 0,
    cancelAction: Boolean = true,
    ignore: Boolean = true,
    type: PathType = if (this is Player) PathType.Smart else PathType.Dumb,
    block: ((Path) -> Unit)? = null
) {
    if (cancelAction) {
        action.cancelAndJoin()
    }

    remove<CancellableContinuation<Unit>>("walk_job")?.cancel()

    if (strategy.reached(tile, size) || withinDistance(tile, strategy, distance)) {
        block?.invoke(Path.EMPTY)
        return
    }
    val handler = events.on<Character, Moved>({ withinDistance(to, strategy, distance) }) {
        remove<CancellableContinuation<Unit>>("walk_job")?.resume(Unit)
    }
    val finishedHandler = events.on<Character, MoveStop>({ it.movement.path.state == Path.State.Complete }) {
        remove<CancellableContinuation<Unit>>("walk_job")?.resume(Unit)
    }
    try {
        if (this is Player) {
            dialogues.clear()
            watch(null)
        }
        if (watch != null) {
            watch(watch)
        }
        movement.set(strategy, type, ignore)
        val path = movement.path
        // Suspend manually to not interfere with actions.
        suspendCancellableCoroutine<Unit> {
            this["walk_job"] = it
        }
        if (cantReach(path)) {
            events.emit(CantReach)
        } else {
            block?.invoke(path)
        }
    } finally {
        if (watch != null) {
            watch(null)
            face(watch)
        }
        events.remove(handler)
        events.remove(finishedHandler)
    }
}

private fun Character.cantReach(path: Path, distance: Int = 0): Boolean {
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
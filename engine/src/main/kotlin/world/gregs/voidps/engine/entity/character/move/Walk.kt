package world.gregs.voidps.engine.entity.character.move

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import world.gregs.voidps.engine.action.Scheduler
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.CantReach
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.MoveStop
import world.gregs.voidps.engine.entity.character.Moving
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.noInterest
import world.gregs.voidps.engine.entity.character.update.visual.player.face
import world.gregs.voidps.engine.entity.character.update.visual.watch
import world.gregs.voidps.engine.entity.remove
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.map.Distance.getNearest
import world.gregs.voidps.engine.map.Overlap
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.path.PathFinder
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.path.PathType
import world.gregs.voidps.engine.path.strat.TileTargetStrategy
import world.gregs.voidps.engine.utility.get
import kotlin.coroutines.resume

fun Character.walkTo(
    target: Any,
    watch: Character? = null,
    distance: Int = 0,
    cancelAction: Boolean = false,
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
    cancelAction: Boolean = false,
    ignore: Boolean = true,
    type: PathType = if (this is Player) PathType.Smart else PathType.Dumb,
    block: ((Path) -> Unit)? = null
) {
    get<Scheduler>().launch {
        awaitWalk(strategy, watch, distance, cancelAction, ignore, type, true, block)
    }
}

suspend fun Character.awaitWalk(
    target: Any,
    watch: Character? = null,
    distance: Int = 0,
    cancelAction: Boolean = false,
    ignore: Boolean = true,
    type: PathType = if (this is Player) PathType.Smart else PathType.Dumb,
    stop: Boolean = true,
    block: ((Path) -> Unit)? = null
) {
    awaitWalk(PathFinder.getStrategy(target), watch, distance, cancelAction, ignore, type, stop, block)
}

/**
 * @param target goal location and if it has been reached
 * @param watch character to watch while moving
 * @param distance distance within [target] to execute [block]
 * @param cancelAction whether to interrupt the current action
 * @param ignore should ignored objects be skipped during path finding
 * @param type path finding algorithm type
 * @param stop when target is reached or continue moving if target moves
 * @param block callback once [target] or target [distance] has been reached
 */
suspend fun Character.awaitWalk(
    target: TileTargetStrategy,
    watch: Character? = null,
    distance: Int = 0,
    cancelAction: Boolean = false,
    ignore: Boolean = true,
    type: PathType = if (this is Player) PathType.Smart else PathType.Dumb,
    stop: Boolean = true,
    block: ((Path) -> Unit)? = null
) {
    if (cancelAction) {
        action.cancelAndJoin()
    }

    remove<CancellableContinuation<Boolean>>("walk_job")?.cancel()

    if (stop && (target.reached(tile, size) || withinDistance(tile, size, target, distance))) {
        block?.invoke(Path.EMPTY)
        return
    }
    val handler = events.on<Character, Moving>({ withinDistance(to, size, target, distance) }) {
        remove<CancellableContinuation<Boolean>>("walk_job")?.resume(true)
    }
    val finishedHandler = events.on<Character, MoveStop>({ it.movement.path.state == Path.State.Complete }) {
        remove<CancellableContinuation<Boolean>>("walk_job")?.resume(true)
    }
    val targetHandler = watch?.events?.on<Character, Moving> {
        movement.path.recalculate()
        remove<CancellableContinuation<Boolean>>("walk_job")?.resume(false)
    }
    try {
        if (this is Player) {
            dialogues.clear()
            watch(null)
        }
        if (watch != null) {
            watch(watch)
        }
        movement.set(target, type, ignore)
        val path = movement.path
        while (true) {
            // Suspend manually to not interfere with actions.
            val reached = suspendCancellableCoroutine<Boolean> {
                this["walk_job"] = it
            }
            if (stop && reached) {
                break
            }
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
        if (targetHandler != null) {
            watch.events.remove(targetHandler)
        }
    }
}

fun Character.cantReach(path: Path, distance: Int = 0): Boolean {
    return path.result is PathResult.Failure || (path.result is PathResult.Partial && !path.strategy.reached(tile, size) && !withinDistance(tile, size, path.strategy, distance))
}

fun withinDistance(tile: Tile, size: Size, target: TileTargetStrategy, distance: Int, walls: Boolean = false, ignore: Boolean = true): Boolean {
    if (Overlap.isUnder(tile, size, target.tile, target.size)) {
        return false
    }
    return distance > 0 && tile.distanceTo(target.tile, target.size) <= distance && tile.withinSight(getNearest(target.tile, target.size, tile), walls = walls, ignore = ignore)
}

fun Player.interact(event: Event) {
    if (!events.emit(event)) {
        noInterest()
    }
}
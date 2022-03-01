package world.gregs.voidps.engine.entity.character.move

import kotlinx.coroutines.suspendCancellableCoroutine
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.CantReach
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.noInterest
import world.gregs.voidps.engine.entity.character.watch
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.map.Distance.getNearest
import world.gregs.voidps.engine.map.Overlap
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
    walkTo(strategy, watch, distance, cancelAction, ignore, type, true, block)
}

suspend fun Character.awaitWalk(
    target: Any,
    watch: Character? = null,
    distance: Int = 0,
    cancelAction: Boolean = false,
    ignore: Boolean = true,
    type: PathType = if (this is Player) PathType.Smart else PathType.Dumb,
    stop: Boolean = true
) = awaitWalk(PathFinder.getStrategy(target), watch, distance, cancelAction, ignore, type, stop)

suspend fun Character.awaitWalk(
    target: TileTargetStrategy,
    watch: Character? = null,
    distance: Int = 0,
    cancelAction: Boolean = false,
    ignore: Boolean = true,
    type: PathType = if (this is Player) PathType.Smart else PathType.Dumb,
    stop: Boolean = true
): Path = suspendCancellableCoroutine { cont ->
    walkTo(target, watch, distance, cancelAction, ignore, type, stop) { path ->
        cont.resume(path)
    }
}


/**
 * @param target goal location and if it has been reached
 * @param watch character to watch while moving
 * @param distance distance within [target] to execute [block]
 * @param cancelAction whether to interrupt the current action
 * @param ignore should ignore objects be skipped during path finding
 * @param type path finding algorithm type
 * @param stop when target is reached or continue moving if target moves
 * @param block callback once [target] or target [distance] has been reached
 */
private fun Character.walkTo(
    target: TileTargetStrategy,
    watch: Character? = null,
    distance: Int = 0,
    cancelAction: Boolean = false,
    ignore: Boolean = this is Player,
    type: PathType = if (this is Player) PathType.Smart else PathType.Dumb,
    stop: Boolean = true,
    block: ((Path) -> Unit)? = null
) = cancelAction(cancelAction) {
    clear("walk_block")

    if (stop && (target.reached(tile, size) || withinDistance(tile, size, target, distance))) {
        block?.invoke(Path.EMPTY)
        return@cancelAction
    }

    this["walk_target"] = target
    this["walk_distance"] = distance
    watch?.getOrPut("walk_followers") { mutableListOf<Character>() }?.add(this)
    if (this is Player) {
        dialogues.clear()
        watch(null)
    }
    if (watch != null) {
        watch(watch)
    }
    movement.set(target, type, ignore)
    val path = movement.path
    walk(path, watch, stop, block)
}

private fun Character.cancelAction(cancelAction: Boolean, block: () -> Unit) {
    if (cancelAction && action.type != ActionType.None) {
        this["walk_cancel"] = block
        action.cancel()
    } else {
        block()
    }
}

private fun Character.walk(
    path: Path,
    watch: Character?,
    stop: Boolean = true,
    block: ((Path) -> Unit)? = null
) {
    this["walk_block"] = { reached: Boolean ->
        if (stop && reached) {
            if (cantReach(path)) {
                events.emit(CantReach)
            } else {
                block?.invoke(path)
            }
            if (watch != null) {
                watch(null)
                face(watch)
            }
            clear("walk_target")
            clear("walk_distance")
            clear("walk_character")
            watch?.get<MutableList<Character>>("walk_followers")?.remove(this)
        } else {
            walk(path, watch, stop, block)
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
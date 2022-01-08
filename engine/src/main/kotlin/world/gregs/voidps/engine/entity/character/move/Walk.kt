package world.gregs.voidps.engine.entity.character.move

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.Moved
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.cantReach
import world.gregs.voidps.engine.entity.character.update.visual.player.face
import world.gregs.voidps.engine.entity.character.update.visual.watch
import world.gregs.voidps.engine.path.PathFinder
import world.gregs.voidps.engine.path.strat.TileTargetStrategy
import kotlin.coroutines.resume

fun Character.walkTo(target: Any, watch: Character? = null, distance: Int = 0, cancelAction: Boolean = true, action: ((Path) -> Unit)? = null) {
    walkTo(PathFinder.getStrategy(target), watch, distance, cancelAction, action)
}

fun Character.walkTo(strategy: TileTargetStrategy, watch: Character? = null, distance: Int = 0, cancelAction: Boolean = true, block: ((Path) -> Unit)? = null) {
    delay(this) {
        awaitWalk(strategy, watch, distance, cancelAction, block)
    }
}

suspend fun Character.awaitWalk(target: Any, watch: Character? = null, distance: Int = 0, cancelAction: Boolean = true, block: ((Path) -> Unit)? = null) {
    awaitWalk(PathFinder.getStrategy(target), watch, distance, cancelAction, block)
}

suspend fun Character.awaitWalk(strategy: TileTargetStrategy, watch: Character? = null, distance: Int = 0, cancelAction: Boolean = true, block: ((Path) -> Unit)? = null) {
    if (cancelAction) {
        action.cancelAndJoin()
    }
    if (strategy.reached(tile, size)) {
        block?.invoke(Path.EMPTY)
        return
    }
    var continuation: CancellableContinuation<Path>? = null
    val handler = events.on<Character, Moved>({ distance > 0 && to.distanceTo(strategy.tile, strategy.size) <= distance }) {
        continuation?.resume(Path.EMPTY)
    }
    try {
        if (this is Player) {
            dialogues.clear()
            watch(null)
        }
        if (watch != null) {
            watch(watch)
        }
        movement.set(strategy, this is Player) { path ->
            if (this is Player && cantReach(path)) {
                cantReach()
                continuation?.cancel()
            } else if (distance == 0) {
                continuation?.resume(path)
            }
        }
        // Suspend manually to not interfere with actions.
        val result = suspendCancellableCoroutine<Path> {
            continuation = it
        }
        block?.invoke(result)
    } finally {
        if (watch != null) {
            watch(null)
            face(watch)
        }
        events.remove(handler)
    }
}

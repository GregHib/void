import kotlinx.coroutines.CancellableContinuation
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.MoveStop
import world.gregs.voidps.engine.entity.character.Moving
import world.gregs.voidps.engine.entity.character.move.Path
import world.gregs.voidps.engine.entity.character.move.withinDistance
import world.gregs.voidps.engine.entity.contains
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.remove
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.path.strat.TileTargetStrategy
import kotlin.coroutines.resume

val Character.walkTarget: TileTargetStrategy
    get() = this["walk_target"]

val Character.walkDistance: Int
    get() = this["walk_distance", 0]

on<Moving>({ it.contains("walk_target") && withinDistance(to, it.size, it.walkTarget, it.walkDistance) }) { character: Character ->
    character.remove<CancellableContinuation<Boolean>>("walk_job")?.resume(true)
}

on<MoveStop>({ it.movement.path.state == Path.State.Complete }) { character: Character ->
    character.remove<CancellableContinuation<Boolean>>("walk_job")?.resume(true)
}

on<Moving>({ it.contains("watchers") }) { character: Character ->
    val watchers: List<Character> = character["walk_watchers"]
    for (watcher in watchers) {
        watcher.movement.path.recalculate()
        watcher.remove<CancellableContinuation<Boolean>>("walk_job")?.resume(false)
    }
}
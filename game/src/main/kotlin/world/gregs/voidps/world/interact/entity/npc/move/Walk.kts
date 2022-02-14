import world.gregs.voidps.engine.action.ActionFinished
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

val Character.walkTarget: TileTargetStrategy
    get() = this["walk_target"]

val Character.walkDistance: Int
    get() = this["walk_distance", 0]

on<Moving>({ it.contains("walk_target") && withinDistance(to, it.size, it.walkTarget, it.walkDistance) }) { character: Character ->
    character.remove<(Boolean) -> Unit>("walk_block")?.invoke(true)
}

on<MoveStop>({ it.movement.path.state == Path.State.Complete }) { character: Character ->
    character.remove<(Boolean) -> Unit>("walk_block")?.invoke(true)
}

on<Moving>({ it.contains("walk_followers") }) { character: Character ->
    val watchers: List<Character> = character["walk_followers"]
    for (watcher in watchers) {
        watcher.movement.path.recalculate()
        watcher.remove<(Boolean) -> Unit>("walk_block")?.invoke(false)
    }
}

on<ActionFinished>({ it.contains("walk_cancel") }) { character: Character ->
    val block: () -> Unit = character.remove("walk_cancel") ?: return@on
    block.invoke()
}
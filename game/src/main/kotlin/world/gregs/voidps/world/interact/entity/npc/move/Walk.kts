import world.gregs.voidps.engine.action.ActionFinished
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.*
import world.gregs.voidps.engine.entity.character.CantReach
import world.gregs.voidps.engine.entity.character.move.Path
import world.gregs.voidps.engine.entity.character.move.cantReach
import world.gregs.voidps.engine.entity.character.move.withinDistance
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.path.strat.TileTargetStrategy

val Character.walkTarget: TileTargetStrategy
    get() = this["walk_target"]

val Character.walkDistance: Int
    get() = this["walk_distance", 0]

on<Moving>({ it.contains("walk_target") && withinDistance(to, it.size, it.walkTarget, it.walkDistance) }) { character: Character ->
    character.completePath(true)
}

on<MoveStop>({ it.movement.path.state == Path.State.Complete }) { character: Character ->
    character.completePath(true)
}

on<Moving>({ it.contains("walk_followers") }) { character: Character ->
    val watchers: List<Character?> = character["walk_followers"]
    for (watcher in watchers) {
        if (watcher == null) {
            continue
        }
        watcher.movement.path.recalculate()
        watcher.completePath(false)
    }
}

on<ActionFinished>({ it.contains("walk_cancel") }) { character: Character ->
    val block: () -> Unit = character.remove("walk_cancel") ?: return@on
    block.invoke()
}

fun Character.completePath(reached: Boolean) {
    val stop: Boolean = getOrNull("walk_stop") ?: return
    if (stop && reached) {
        val path: Path = get("walk_path")
        if (cantReach(path)) {
            events.emit(CantReach)
        } else {
            val block: ((Path) -> Unit)? = getOrNull("walk_block")
            block?.invoke(path)
        }
        val watch: Character? = getOrNull("walk_watch")
        if (watch != null) {
            watch(null)
            face(watch)
        }
        clear("walk_stop")
        clear("walk_path")
        clear("walk_block")
        clear("walk_watch")

        clear("walk_target")
        clear("walk_distance")
        clear("walk_character")
        watch?.get<MutableList<Character>>("walk_followers")?.remove(this)
    }
}

package rs.dusk.world.entity

import rs.dusk.engine.model.entity.Entity
import rs.dusk.engine.model.entity.character.player.Player
import rs.dusk.engine.model.entity.character.walkTo
import rs.dusk.engine.path.PathFinder
import rs.dusk.engine.path.PathResult
import rs.dusk.utility.get

fun Player.approach(target: Entity, action: (PathResult) -> Unit) = walkTo(target) { result ->
    val pf: PathFinder = get()
    if (result is PathResult.Failure || !pf.getStrategy(target).reached(tile, size)) {
        println("You can't reach that.")
        return@walkTo
    }

    action(result)
}
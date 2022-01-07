package world.gregs.voidps.engine.entity.character.move

import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.Suspension
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.watch
import world.gregs.voidps.engine.map.collision.collision
import world.gregs.voidps.engine.path.PathFinder
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.path.algorithm.AvoidAlgorithm
import world.gregs.voidps.engine.path.traverse.traversal
import world.gregs.voidps.engine.utility.get

fun Character.avoid(target: Character) {
    val strategy = PathFinder.getStrategy(target)
    val pathfinder: AvoidAlgorithm = get()
    action(ActionType.Movement) {
        try {
            movement.set(strategy, this@avoid is Player) { path ->
                if (path.result is PathResult.Success) {
                    this.resume()
                }
            }
            watch(target)
            pathfinder.find(tile, size, movement.path, traversal, collision)
            await<Unit>(Suspension.Movement)
            delay(4)
        } finally {
            watch(null)
        }
    }
}
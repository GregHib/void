package world.gregs.voidps.engine.entity.character.move

import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.path.PathType
import world.gregs.voidps.engine.path.strat.DistanceFromTargetStrategy

fun Character.retreat(target: Character, distance: Int = 25) {
    action(ActionType.Movement) {
        val strategy = DistanceFromTargetStrategy(target, distance)
        while (isActive && !strategy.reached(tile, size)) {
            movement.clear()
            awaitWalk(strategy, watch = target, type = PathType.Retreat, cancelAction = false)
        }
    }
}
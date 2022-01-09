package world.gregs.voidps.engine.entity.character.move

import world.gregs.voidps.engine.action.ActionStarted
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.path.PathType

fun Character.follow(target: Character) {
    action(ActionType.Follow) {
        val handler = target.events.on<Character, ActionStarted>({ type == ActionType.Teleport || type == ActionType.Climb || type == ActionType.Logout || type == ActionType.Dying }) {
            cancel()
        }
        try {
            val targetStrategy = target.followTarget
            awaitWalk(targetStrategy, target, cancelAction = false, type = PathType.Follow, stop = false)
        } finally {
            target.events.remove(handler)
        }
    }
}
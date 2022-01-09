package world.gregs.voidps.engine.entity.character.move

import world.gregs.voidps.engine.action.ActionStarted
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.update.visual.watch
import world.gregs.voidps.engine.path.PathType

fun Character.follow(target: Character) {
    action(ActionType.Follow) {
        watch(target)
        val handler = target.events.on<Character, ActionStarted>({ type == ActionType.Teleport || type == ActionType.Climb || type == ActionType.Logout || type == ActionType.Dying }) {
            cancel()
        }
        try {
            val targetStrategy = target.followTarget
            while (isActive) {
                if (!targetStrategy.reached(tile, size)) {
                    movement.set(targetStrategy, PathType.Follow)
                }
                delay()
            }
        } finally {
            watch(null)
            target.events.remove(handler)
        }
    }
}
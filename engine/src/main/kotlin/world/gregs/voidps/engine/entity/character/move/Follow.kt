package world.gregs.voidps.engine.entity.character.move

import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.path.PathType

fun Character.follow(target: Character) {
    action(ActionType.Follow) {
        try {
            val targetStrategy = target.followTarget
            awaitWalk(targetStrategy, target, type = PathType.Follow, stop = false)
        } finally {
            clearWalk()
        }
    }
}
package world.gregs.voidps.engine.entity.character.move

import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.getOrPut

fun Player.follow(target: Character) {
    action(ActionType.Follow) {
        try {
            while (isActive) {
                walkTo(target.movement.previousTile)
                delay(1)
            }
        } finally {
            clearWalk()
        }
    }
}
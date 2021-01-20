package world.gregs.void.world.interact.entity.player

import world.gregs.void.engine.action.ActionType
import world.gregs.void.engine.action.action
import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.engine.entity.character.player.PlayerOption
import world.gregs.void.engine.entity.character.update.visual.watch
import world.gregs.void.engine.event.then
import world.gregs.void.engine.event.where
import world.gregs.void.engine.path.PathFinder
import world.gregs.void.utility.inject

val path: PathFinder by inject()

PlayerOption where { option == "Follow" } then {
    val follower = player
    follower.watch(target)
    follower.action(ActionType.Movement) {
        try {
            while (!disengageTarget(target)) {
                if (!player.reached(target)) {
                    player.movement.clear()
                    path.find(player, target.followTarget, false)
                }
                delay()
            }
        } finally {
            follower.watch(null)
        }
    }
}

fun Player.reached(target: Player): Boolean {
    return target.followTarget.reached(tile, size)
}

fun disengageTarget(target: Player): Boolean {
    val action = target.action.type
    return action == ActionType.Teleport || action == ActionType.Logout
}
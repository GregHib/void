package world.gregs.voidps.world.interact.entity.player

import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerOption
import world.gregs.voidps.engine.entity.character.update.visual.watch
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.path.PathFinder
import world.gregs.voidps.utility.inject

val path: PathFinder by inject()

on<PlayerOption>({ option == "Follow" }) { player: Player ->
    val follower = player
    follower.watch(target)
    follower.action(ActionType.Follow) {
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
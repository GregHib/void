package rs.dusk.world.interact.entity.player

import rs.dusk.engine.action.ActionType
import rs.dusk.engine.action.action
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.PlayerOption
import rs.dusk.engine.entity.character.update.visual.watch
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.engine.path.PathFinder
import rs.dusk.utility.inject

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
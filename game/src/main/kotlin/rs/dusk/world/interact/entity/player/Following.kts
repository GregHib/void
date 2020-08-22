package rs.dusk.world.interact.entity.player

import rs.dusk.engine.action.ActionType
import rs.dusk.engine.action.action
import rs.dusk.engine.entity.character.move.PlayerMoved
import rs.dusk.engine.entity.character.player.PlayerOption
import rs.dusk.engine.entity.character.update.visual.player.getFace
import rs.dusk.engine.entity.character.update.visual.watch
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.engine.path.PathFinder
import rs.dusk.utility.inject

val pf: PathFinder by inject()

PlayerMoved then {
    val delta = to.delta(from)
    player.getFace().apply {
        deltaX = delta.x.coerceIn(-1, 1)
        deltaY = delta.y.coerceIn(-1, 1)
    }
}

PlayerOption where { option == "Follow" } then {
    val follower = player
    follower.watch(target)
    follower.action(ActionType.Movement) {
        try {
            while (target.action.type != ActionType.Teleport && target.action.type != ActionType.Logout) {
                if(!target.followTarget.reached(player.tile, player.size)) {
                    player.movement.clear()
                    pf.find(player, target.followTarget, false)
                }
                delay()
            }
        } finally {
            follower.watch(null)
        }
    }
}
package rs.dusk.world.interact.entity.player

import rs.dusk.engine.action.ActionType
import rs.dusk.engine.action.action
import rs.dusk.engine.entity.character.move.PlayerMoved
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.PlayerOption
import rs.dusk.engine.entity.character.update.visual.player.getFace
import rs.dusk.engine.entity.character.update.visual.watch
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.engine.path.PathFinder
import rs.dusk.handle.PlayerOptionMessageHandler.Companion.getTileBehind
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
            while (delay() && target.action.type != ActionType.Teleport && target.action.type != ActionType.Logout) {
                if(!pf.getStrategy(getTileBehind(target)).reached(player.tile, player.size)) {
                    moveTo(player, target)
                }
            }
        } finally {
            follower.watch(null)
        }
    }
}

fun moveTo(player: Player, target: Player) {
    val tile = getTileBehind(target)
    player.movement.clear()
    pf.find(player, tile, false)
}
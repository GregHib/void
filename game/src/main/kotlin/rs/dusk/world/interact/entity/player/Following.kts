package rs.dusk.world.interact.entity.player

import rs.dusk.engine.action.ActionType
import rs.dusk.engine.action.action
import rs.dusk.engine.entity.character.move.Moved
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.PlayerOption
import rs.dusk.engine.entity.character.update.visual.player.getFace
import rs.dusk.engine.entity.character.update.visual.watch
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.engine.path.PathFinder
import rs.dusk.utility.inject

val pf: PathFinder by inject()

Moved where { entity is Player } then {
    val player = entity as Player
    val delta = player.movement.delta
    player.getFace().apply {
        deltaX = delta.x.coerceIn(-1, 1)
        deltaY = delta.y.coerceIn(-1, 1)
    }
}

PlayerOption where { option == "Follow" } then {
    player.watch(target)
    player.action(ActionType.Movement) {
        try {
            while (delay()) {
                // TODO break on logout
                if(target.action.type == ActionType.Teleport) {
                    break
                }
                val direction = target.getFace().getDirection()
                val tile = target.tile.minus(direction.delta)
                player.movement.clear()
                pf.find(player, tile, false)
            }
        } finally {
            player.watch(null)
        }
    }
}
package rs.dusk.world.entity.player

import rs.dusk.engine.action.ActionType
import rs.dusk.engine.action.action
import rs.dusk.engine.client.verify.verify
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.path.PathFinder
import rs.dusk.engine.path.PathResult
import rs.dusk.network.rs.codec.game.decode.message.WalkMapMessage
import rs.dusk.network.rs.codec.game.decode.message.WalkMiniMapMessage
import rs.dusk.utility.inject

val pf: PathFinder by inject()

WalkMapMessage verify { player ->
    walk(player, x, y)
}

WalkMiniMapMessage verify { player ->
    walk(player, x, y)
}

fun walk(player: Player, x: Int, y: Int) {
    player.action(ActionType.Movement) {
        try {
            val result = pf.find(player, player.tile.copy(x = x, y = y))
            if (result is PathResult.Failure) {
                println("You can't reach that.")
            } else {
                while (delay() && awaitInterfaces()) {
                    if (player.movement.steps.isEmpty()) {
                        break
                    }
                }
            }
        } finally {
            player.movement.clear()
        }
    }
}
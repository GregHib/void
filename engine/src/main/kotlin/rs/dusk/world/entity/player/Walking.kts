package rs.dusk.world.entity.player

import rs.dusk.engine.client.verify.verify
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.entity.index.walkTo
import rs.dusk.engine.path.PathResult
import rs.dusk.network.rs.codec.game.decode.message.WalkMapMessage
import rs.dusk.network.rs.codec.game.decode.message.WalkMiniMapMessage

WalkMapMessage verify { player ->
    walk(player, x, y)
}

WalkMiniMapMessage verify { player ->
    walk(player, x, y)
}

fun walk(player: Player, x: Int, y: Int) {
    player.walkTo(player.tile.copy(x = x, y = y)) { result ->
        if (result is PathResult.Failure) {
            println("You can't reach that.")
            return@walkTo
        }
    }
}
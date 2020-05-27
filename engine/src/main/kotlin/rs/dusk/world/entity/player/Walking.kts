import rs.dusk.engine.client.verify.verify
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.world.map.collision.Collisions
import rs.dusk.engine.path.PathFinder
import rs.dusk.engine.path.PathResult
import rs.dusk.network.rs.codec.game.decode.message.WalkMapMessage
import rs.dusk.network.rs.codec.game.decode.message.WalkMiniMapMessage
import rs.dusk.utility.inject

val bus: EventBus by inject()
val collisions: Collisions by inject()
val pf: PathFinder by inject()

WalkMapMessage verify { player ->
    walk(player, x, y)
}

WalkMiniMapMessage verify { player ->
    walk(player, x, y)
}

fun walk(player: Player, x: Int, y: Int) {
    player.movement.steps.clear()
    player.movement.reset()
    val path = pf.find(player, player.tile.copy(x = x, y = y))
    when (path) {
        is PathResult.Success.Partial -> {
            println("Almost")
        }
        is PathResult.Success -> {
            println("Woop")
        }
        PathResult.Failure -> {
            println("You can't reach this.")
        }
    }
}
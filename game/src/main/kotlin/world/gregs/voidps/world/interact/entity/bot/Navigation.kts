package world.gregs.voidps.world.interact.entity.bot

import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.move.PlayerMoved
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.login.PlayerRegistered
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.nav.NavigationGraph
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.path.TargetStrategy
import world.gregs.voidps.engine.path.algorithm.BreadthFirstSearch
import world.gregs.voidps.utility.inject

val graph: NavigationGraph by inject()
val bfs: BreadthFirstSearch by inject()

PlayerRegistered then {
    findNearest(player)
}

PlayerMoved then {
    if (from.distanceTo(to) > 2) {
        findNearest(player)
    } else {
        var nearest = player.movement.nearestWaypoint
        for (index in graph[nearest]) {
            val node = graph[index] ?: continue
            if (player.tile.distanceTo(node.start) < player.tile.distanceTo(nearest)) {
                nearest = node.start
            }
        }
        player.movement.nearestWaypoint = nearest
    }
}

fun findNearest(player: Player) {
    val result = bfs.find(player.tile, player.size, player.movement, object : TargetStrategy {
        override val tile: Tile
            get() = player.tile
        override val size: Size
            get() = player.size

        override fun reached(tile: Tile, size: Size): Boolean {
            return graph.contains(tile)
        }
    }, player.movement.traversal)
    if (result is PathResult.Success) {
        player.movement.steps.clear()
        player.movement.nearestWaypoint = result.last
    } else {
        println("Couldn't find nearby waypoint $player")
    }
}

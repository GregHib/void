package world.gregs.voidps.world.interact.entity.bot

import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.character.Moved
import world.gregs.voidps.engine.entity.character.move.Movement
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.nav.Edge
import world.gregs.voidps.engine.map.nav.NavigationGraph
import world.gregs.voidps.engine.path.algorithm.BreadthFirstSearch
import world.gregs.voidps.engine.path.strat.TileTargetStrategy
import world.gregs.voidps.network.instruct.Walk
import world.gregs.voidps.utility.inject

val graph: NavigationGraph by inject()
val bfs: BreadthFirstSearch by inject()

on<Registered> { player: Player ->
    findNearest(player)
}

on<Unregistered> { player: Player ->
    graph.remove(player)
}

on<Moved> { player: Player ->
    findNearest(player)
}

val movement = Movement()

fun findNearest(player: Player) {
    val edges = graph.get(player)
    edges.clear()
    movement.reset()
    bfs.find(player.tile, player.size, movement, object : TileTargetStrategy {
        override val tile: Tile
            get() = player.tile
        override val size: Size
            get() = player.size

        override fun reached(tile: Tile, size: Size): Boolean {
            val distance = this.tile.distanceTo(tile)
            if (distance > 20) {
                return true
            }
            if (graph.contains(tile)) {
                edges.add(Edge("", player, tile, distance, listOf(Walk(tile.x, tile.y))))
            }
            return false
        }
    }, player.movement.traversal)
}
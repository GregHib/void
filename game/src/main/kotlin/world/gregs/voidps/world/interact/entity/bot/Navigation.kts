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
    /*if (from.distanceTo(to) > 2) {
    } else {
        val edges = graph.get(player)
        val it = edges.iterator()
        while (it.hasNext()) {
            val edge = it.next()
            val tile = edge.start as? Tile ?: continue
            if (edge.start is Tile && player.tile.distanceTo(tile) > 20) {
                println("Remove $edge")
                it.remove()
            }
        }
        val toAdd = mutableListOf<Edge>()
        for (parent in edges) {
            for (edge in graph.getAdjacent(parent)) {
                val tile = edge.start as? Tile ?: continue
                if (player.tile.distanceTo(tile) <= 20) {
                    println("Add $edge")
                    toAdd.add(edge)
                }
            }
        }
        edges.addAll(toAdd)
    }*/
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
                edges.add(Edge("", player, tile, distance))
            }
            return false
        }
    }, player.movement.traversal)
}
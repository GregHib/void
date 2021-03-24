package world.gregs.voidps.world.interact.entity.bot

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.Moved
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.login.PlayerRegistered
import world.gregs.voidps.engine.entity.character.player.logout.PlayerUnregistered
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.nav.Edge
import world.gregs.voidps.engine.map.nav.NavigationGraph
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.path.algorithm.BreadthFirstSearch
import world.gregs.voidps.engine.path.strat.TileTargetStrategy
import world.gregs.voidps.utility.inject

val graph: NavigationGraph by inject()
val bfs: BreadthFirstSearch by inject()

on<PlayerRegistered> { player: Player ->
    findNearest(player)
}

on<PlayerUnregistered> { player: Player ->
    graph.remove(player)
}

on<Moved> { player: Player ->
    if (from.distanceTo(to) > 2) {
        findNearest(player)
    } else {
        val old = player.movement.nearestWaypoint ?: return@on
        var nearest = old
        val tile = nearest.end as Tile
        for (edge in graph.getAdjacent(tile)) {
            if (player.tile.distanceTo(edge.start as Tile) < player.tile.distanceTo(tile)) {
                nearest = edge
            }
        }
        if (old != nearest) {
            updateGraph(player, old, nearest)
        }
    }
}

fun findNearest(player: Player) {
    val result = bfs.find(player.tile, player.size, player.movement, object : TileTargetStrategy {
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
        val last = player.movement.nearestWaypoint
        val edge = Edge(player, result.last, player.tile.distanceTo(result.last))
        updateGraph(player, last, edge)
    } else {
        println("Couldn't find nearby waypoint $player")
    }
}

fun updateGraph(player: Player, old: Edge?, new: Edge) {
    val set = graph.get(player) ?: ObjectOpenHashSet()
    set.remove(old)
    set.add(new)
    player.movement.nearestWaypoint = new
}
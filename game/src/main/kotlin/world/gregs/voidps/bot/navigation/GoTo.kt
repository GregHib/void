package world.gregs.voidps.bot.navigation

import world.gregs.voidps.engine.entity.character.player.Bot
import world.gregs.voidps.engine.entity.getOrNull
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.MapArea
import world.gregs.voidps.engine.map.nav.NavigationGraph
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.path.algorithm.Dijkstra
import world.gregs.voidps.engine.path.strat.NodeTargetStrategy
import world.gregs.voidps.engine.path.traverse.EdgeTraversal
import world.gregs.voidps.utility.get

suspend fun Bot.goToNearest(tag: String) {
    val current: MapArea? = this.getOrNull("area")
    if (current?.tags?.contains(tag) == true) {
        return
    }
    val graph: NavigationGraph = get()
    var last: MapArea? = null
    val result = goTo(object : NodeTargetStrategy() {
        override fun reached(node: Any): Boolean {
            if (node !is Tile) {
                return false
            }
            for (area in graph.areas(node)) {
                if (area.tags.contains(tag)) {
                    last = area
                    return true
                }
            }
            return false
        }
    })
    if (result !is PathResult.Failure && last != null) {
        this["area"] = last!!
    }
}

suspend fun Bot.goToArea(map: MapArea) {
    val current: MapArea? = this.getOrNull("area")
    if (current == map) {
        return
    }
    val result = goTo(object : NodeTargetStrategy() {
        override fun reached(node: Any): Boolean {
            return node is Tile && node in map.area
        }
    })
    if (result !is PathResult.Failure) {
        this["area"] = map
    }
}

private suspend fun Bot.goTo(strategy: NodeTargetStrategy): PathResult {
    player.movement.waypoints.clear()
    val result = get<Dijkstra>().find(player, strategy, EdgeTraversal())
    this["navigating"] = result is PathResult.Failure
    if (result !is PathResult.Failure) {
        navigate()
    }
    return result
}

private suspend fun Bot.navigate() {
    // TODO if low energy, rest
    val waypoints = player.movement.waypoints.toMutableList().iterator()
    while (waypoints.hasNext()) {
        val waypoint = waypoints.next()
        for (step in waypoint.steps) {
            this.step = step
            player.instructions.emit(step)
            await<Unit>("move")
        }
        waypoints.remove()
    }
    player["navigating"] = false
}
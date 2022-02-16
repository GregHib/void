package world.gregs.voidps.bot.navigation

import kotlinx.coroutines.withTimeoutOrNull
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.player.Bot
import world.gregs.voidps.engine.entity.getOrNull
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.MapArea
import world.gregs.voidps.engine.map.nav.NavigationGraph
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.path.algorithm.Dijkstra
import world.gregs.voidps.engine.path.strat.NodeTargetStrategy
import world.gregs.voidps.engine.path.traverse.EdgeTraversal
import world.gregs.voidps.engine.utility.TICKS
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.network.instruct.InteractInterface
import world.gregs.voidps.network.instruct.InteractNPC
import world.gregs.voidps.network.instruct.InteractObject
import world.gregs.voidps.world.interact.entity.player.energy.energyPercent

suspend fun Bot.goToNearest(tag: String) = goToNearest { it.tags.contains(tag) }

suspend fun Bot.goToNearest(block: (MapArea) -> Boolean): Boolean {
    val current: MapArea? = this.getOrNull("area")
    if (current != null && block.invoke(current)) {
        return true
    }
    val graph: NavigationGraph = get()
    var last: MapArea? = null
    val result = goTo(object : NodeTargetStrategy() {
        override fun reached(node: Any): Boolean {
            if (node !is Tile) {
                return false
            }
            for (area in graph.areas(node)) {
                if (block(area)) {
                    last = area
                    return true
                }
            }
            return false
        }
    })
    assert(result is PathResult.Success) { "Unable to find path." }
    assert(last != null) { "Unable to find path target." }
    if (result !is PathResult.Failure && last != null) {
        this["area"] = last!!
        return true
    }
    return false
}

suspend fun Bot.goToArea(map: MapArea) {
    if (map.area.contains(player.tile)) {
        return
    }
    val result = goTo(object : NodeTargetStrategy() {
        override fun reached(node: Any): Boolean {
            return node is Tile && node in map.area
        }
    })
    if (result !is PathResult.Failure) {
        this["area"] = map
    } else {
        throw IllegalStateException("Failed to find path to ${map.name} from ${player.tile}")
    }
}

private suspend fun Bot.goTo(strategy: NodeTargetStrategy): PathResult {
    player.movement.waypoints.clear()
    if (strategy.reached(player.tile)) {
        return PathResult.Success(player.tile)
    }
    val result = get<Dijkstra>().find(player, strategy, EdgeTraversal())
    this["navigating"] = result is PathResult.Failure
    if (result !is PathResult.Failure) {
        navigate()
    }
    return result
}

private suspend fun Bot.rest() {
    val musician = player.viewport.npcs.firstOrNull { it.def.options.contains("Listen-to") }
    if (musician != null && player.tile.distanceTo(musician) < 10) {
        player.instructions.emit(InteractNPC(npcIndex = 49, option = musician.def.options.indexOfFirst { it == "Listen-to" } + 1))
        repeat(32) {
            await("tick")
        }
    } else {
        player.instructions.emit(InteractInterface(interfaceId = 750, componentId = 1, itemId = -1, itemSlot = -1, option = 1))
        repeat(50) {
            await("tick")
        }
    }
}

private suspend fun Bot.run() {
    player.instructions.emit(InteractInterface(interfaceId = 750, componentId = 1, itemId = -1, itemSlot = -1, option = 0))
}

private suspend fun Bot.navigate() {
    val waypoints = player.movement.waypoints.toMutableList().iterator()
    while (waypoints.hasNext()) {
        val waypoint = waypoints.next()
        for (step in waypoint.steps) {
            if (player.energyPercent() <= 25) {
                rest()
            } else if (!player.running) {
                run()
            }
            this.step = step
            player.instructions.emit(step)
            withTimeoutOrNull(TICKS.toMillis(20)) {
                if (step is InteractObject && get<Objects>()[player.tile.copy(step.x, step.y), step.objectId] == null) {
                    await("tick")
                } else {
                    await("move")
                }
            }
        }
        waypoints.remove()
    }
    player["navigating"] = false
}
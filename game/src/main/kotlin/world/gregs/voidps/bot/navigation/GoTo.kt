package world.gregs.voidps.bot.navigation

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
import world.gregs.voidps.network.instruct.InteractInterface
import world.gregs.voidps.network.instruct.InteractNPC
import world.gregs.voidps.network.instruct.InteractObject
import world.gregs.voidps.utility.get
import world.gregs.voidps.world.interact.entity.player.energy.energyPercent

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

private suspend fun Bot.rest() {
    val musician = player.viewport.npcs.current.firstOrNull { it.def.options.contains("Listen-to") }
    if (musician != null && player.tile.distanceTo(musician.tile) < 10) {
        player.instructions.emit(InteractNPC(npcIndex = 49, option = musician.def.options.indexOfFirst { it == "Listen-to" } + 1))
        repeat(32) {
            await<Unit>("tick")
        }
    } else {
        player.instructions.emit(InteractInterface(interfaceId = 750, componentId = 1, itemId = -1, itemSlot = -1, option = 1))
        repeat(50) {
            await<Unit>("tick")
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
            // TODO proper solution for validation failure
            if (step is InteractObject && get<Objects>()[player.tile.copy(step.x, step.y), step.objectId] == null) {
                await<Unit>("tick")
            } else {
                await<Unit>("move")
            }
        }
        waypoints.remove()
    }
    player["navigating"] = false
}
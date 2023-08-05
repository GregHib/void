package world.gregs.voidps.bot.navigation

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.withTimeoutOrNull
import world.gregs.voidps.bot.Bot
import world.gregs.voidps.bot.navigation.graph.Edge
import world.gregs.voidps.bot.navigation.graph.NavigationGraph
import world.gregs.voidps.bot.navigation.graph.waypoints
import world.gregs.voidps.bot.path.*
import world.gregs.voidps.engine.client.update.view.Viewport.Companion.VIEW_RADIUS
import world.gregs.voidps.engine.data.definition.AreaDefinition
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timer.TICKS
import world.gregs.voidps.network.instruct.InteractInterface
import world.gregs.voidps.network.instruct.InteractNPC
import world.gregs.voidps.network.instruct.InteractObject
import world.gregs.voidps.network.instruct.Walk
import world.gregs.voidps.type.Tile
import world.gregs.voidps.world.interact.entity.player.energy.energyPercent

private val logger = InlineLogger()

suspend fun Bot.goToNearest(tag: String) = goToNearest { it.tags.contains(tag) }

suspend fun Bot.goToNearest(block: (AreaDefinition) -> Boolean): Boolean {
    val current: AreaDefinition? = this.getOrNull("area")
    if (current != null && block.invoke(current)) {
        return true
    }
    val graph: NavigationGraph = get()
    val strategy = ConditionalStrategy(graph, block)
    val result = goTo(strategy)
    val area: AreaDefinition? = strategy.area
    assert(result != null) { "Unable to find path." }
    assert(area != null) { "Unable to find path target." }
    if (result != null && area != null) {
        this["area"] = area
        return true
    }
    return false
}

suspend fun Bot.goToArea(map: AreaDefinition) {
    if (map.area.contains(player.tile)) {
        return
    }
    val result = goTo(AreaStrategy(map.area))
    if (result != null) {
        this["area"] = map
    } else {
        throw IllegalStateException("Failed to find path to ${map.name} from ${player.tile}")
    }
}

private suspend fun Bot.goTo(strategy: NodeTargetStrategy): Tile? {
    player.waypoints.clear()
    if (strategy.reached(player.tile)) {
        return player.tile
    }

    updateGraph(this)
    val result = get<Dijkstra>().find(player, strategy, EdgeTraversal())
    this["navigating"] = result == null
    if (result != null) {
        navigate()
    }
    return result
}

private fun updateGraph(bot: Bot) {
    val graph: NavigationGraph = get()
    val edges = graph.get(bot.player)
    edges.clear()
    graph.nodes.filter { it is Tile && it.within(bot.tile, 20) }.forEach {
        val tile = it as Tile
        val distance = tile.distanceTo(bot.tile)
        edges.add(Edge("", bot, tile, distance, listOf(Walk(tile.x, tile.y))))
    }
}

private suspend fun Bot.rest() {
    val musician = get<NPCs>().firstOrNull { it.tile.within(player.tile, VIEW_RADIUS) && it.def.options.contains("Listen-to") }
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
    val waypoints = player.waypoints.toMutableList().iterator()
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
            val timeout = withTimeoutOrNull(TICKS.toMillis(20)) {
                if (step is InteractObject && get<GameObjects>()[player.tile.copy(step.x, step.y), step.objectId] == null) {
                    await("tick")
                } else {
                    await("move")
                }
            }
            if (timeout == null && player["debug", false]) {
                logger.debug { "Bot $player got stuck at $step $waypoint" }
            }
        }
        waypoints.remove()
    }
    player["navigating"] = false
}
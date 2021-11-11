package world.gregs.voidps.bot.navigation

import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.Moved
import world.gregs.voidps.engine.entity.character.move.Movement
import world.gregs.voidps.engine.entity.character.move.Path
import world.gregs.voidps.engine.entity.character.player.Bot
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.nav.Edge
import world.gregs.voidps.engine.map.nav.NavigationGraph
import world.gregs.voidps.engine.path.algorithm.BreadthFirstSearch
import world.gregs.voidps.engine.path.strat.TileTargetStrategy
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.network.instruct.Walk

val graph: NavigationGraph by inject()
val bfs: BreadthFirstSearch by inject()

on<Registered> { bot: Bot ->
    findNearest(bot)
}

on<Unregistered> { bot: Bot ->
    graph.remove(bot.player)
}

on<Moved> { bot: Bot ->
    findNearest(bot)
}

val movement = Movement()

fun findNearest(bot: Bot) {
    val edges = graph.get(bot.player)
    edges.clear()
    findNodes(bot.player) { tile, distance ->
        edges.add(Edge("", bot, tile, distance, listOf(Walk(tile.x, tile.y))))
        false
    }
}

fun findNodes(character: Character, onNode: (Tile, Int) -> Boolean) {
    movement.reset()
    bfs.find(character.tile, character.size, Path(object : TileTargetStrategy {
        override val tile: Tile
            get() = character.tile
        override val size: Size
            get() = character.size

        override fun reached(tile: Tile, size: Size): Boolean {
            val distance = this.tile.distanceTo(tile)
            if (distance > 20) {
                return true
            }
            if (graph.contains(tile)) {
                return onNode(tile, distance)
            }
            return false
        }
    }), character.movement.traversal)
}
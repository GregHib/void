package world.gregs.voidps.world.interact.entity.bot

import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.Moved
import world.gregs.voidps.engine.entity.character.move.Movement
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.set
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
    var first = true
    findNodes(player) { tile, distance ->
        if (first) {
            player["nearest_node"] = tile
            first = false
        }
        edges.add(Edge("", player, tile, distance, listOf(Walk(tile.x, tile.y))))
        false
    }
}

on<Registered>({ it.def.has("shop") }) { npc: NPC ->
    findNodes(npc) { tile, _ ->
        npc["nearest_node"] = tile
        true
    }
}

fun findNodes(character: Character, onNode: (Tile, Int) -> Boolean) {
    movement.reset()
    bfs.find(character.tile, character.size, movement, object : TileTargetStrategy {
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
    }, character.movement.traversal)
}
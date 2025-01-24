package world.gregs.voidps.bot

import world.gregs.voidps.bot.navigation.await
import world.gregs.voidps.bot.path.Dijkstra
import world.gregs.voidps.bot.path.EdgeTraversal
import world.gregs.voidps.bot.path.NodeTargetStrategy
import world.gregs.voidps.engine.client.ui.event.adminCommand
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.worldSpawn
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inject
import world.gregs.voidps.network.client.instruction.Walk
import world.gregs.voidps.type.Tile
import kotlin.system.measureNanoTime

val tasks: TaskManager by inject()

worldSpawn {
    val task = Task(
        name = "walk randomly",
        block = {
            while (true) {
                val tile = tile.toCuboid(10).random()
                instructions.send(Walk(tile.x, tile.y))
                bot.await("tick")
            }
        },
        area = null,
        spaces = Int.MAX_VALUE,
        requirements = emptyList()
    )
    if (Settings["bots.idle", "nothing"] == "randomWalk") {
        tasks.idle(task)
    }
}

adminCommand("walkToBank") {
    val east = Tile(3179, 3433).toCuboid(15, 14)
    val west = Tile(3250, 3417).toCuboid(7, 8)
    val dijkstra: Dijkstra = get()
    val strategy = object : NodeTargetStrategy() {
        override fun reached(node: Any): Boolean {
            return if (node is Tile) east.contains(node) || west.contains(node) else false
        }
    }
    println("Path took ${
        measureNanoTime {
            dijkstra.find(player, strategy, EdgeTraversal())
        }
    }ns")
    /*player.action { FIXME
        var first = true
        while (player.waypoints.isNotEmpty()) {
            val next = player.waypoints.poll()
            suspendCoroutine<Unit> { cont ->
                val tile = if (first && !player.tile.within(next.end as Tile, 20)) {
                    next.start
                } else {
                    next.end
                } as Tile
                first = false
                scheduler.add {
                    player.walkTo(tile)
                }
            }
        }
    }*/
}
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.variable.IntVariable
import world.gregs.voidps.engine.client.variable.Variable
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.Command
import world.gregs.voidps.engine.entity.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.entity.obj.spawnObject
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.Area2D
import world.gregs.voidps.engine.path.TraversalType
import world.gregs.voidps.engine.path.algorithm.Dijkstra
import world.gregs.voidps.engine.path.strat.NodeTargetStrategy
import world.gregs.voidps.engine.path.traverse.EdgeTraversal
import world.gregs.voidps.engine.path.traverse.SmallTraversal
import world.gregs.voidps.network.encode.sendContainerItems
import world.gregs.voidps.utility.get
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.system.measureNanoTime

IntVariable(1109, Variable.Type.VARBIT).register("one")
IntVariable(1112, Variable.Type.VARBIT).register("two")
IntVariable(1113, Variable.Type.VARBIT).register("three")
IntVariable(1241, Variable.Type.VARBIT).register("four")
IntVariable(1242, Variable.Type.VARBIT).register("five")
IntVariable(741, Variable.Type.VARBIT).register("six")
IntVariable(743, Variable.Type.VARBIT).register("seven")
IntVariable(744, Variable.Type.VARBIT).register("eight")

on<Command>({ prefix == "test" }) { player: Player ->
    val traversal = SmallTraversal(TraversalType.Land, false, get())
    println(traversal.blocked(player.tile, Direction.NONE))
}

on<Command>({ prefix == "walkToBank" }) { player: Player ->
    val east = Area2D(Tile(3179, 3433), 15, 14)
    val west = Area2D(Tile(3250, 3417), 7, 8)
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
    player.action {
        var first = true
        while (player.movement.waypoints.isNotEmpty()) {
            val next = player.movement.waypoints.poll()
            suspendCoroutine<Unit> { cont ->
                val tile = if(first && !player.tile.within(next.end as Tile, 20)) {
                    next.start
                } else {
                    next.end
                } as Tile
                first = false
                player.walkTo(tile) {
                    cont.resume(Unit)
                }
            }
        }
    }
}

on<Command>({ prefix == "sendItems" }) { player: Player ->
    player.sendContainerItems(90, IntArray(28) { 995 }, IntArray(28) { 1 }, false)
    player.sendContainerItems(90, IntArray(28) { 11694 }, IntArray(28) { 1 }, true)
}

on<Command>({ prefix == "obj" }) { player: Player ->
    if (content.isNotBlank()) {
        val parts = content.split(" ")
        val id = parts.getOrNull(0)?.toIntOrNull()
        val type = 10
        if (id != null) {
            val rotation = parts.getOrNull(1)?.toIntOrNull() ?: 0
            spawnObject(id, player.tile.addY(2), 10, rotation, 10, null)
            spawnObject(id, player.tile.addY(2), 22, rotation, 10, null)
        } else {
            val definitions = get<ObjectDefinitions>()
            val id = definitions.getId(content)
            if (id >= 0) {
                spawnObject(id, player.tile, type, 0, 10, null)
            }
        }
    } else {
        get<Objects>()[player.tile].forEach {
            println(it.id)
        }
    }
}

on<Command>({ prefix == "tree" }) { player: Player ->
    val parts = content.split(" ")
    val tree = parts[0].toInt()
    val stump = parts[1].toInt()
    val type = parts.getOrNull(2)?.toIntOrNull() ?: 10
    player.action {
        spawnObject(tree, player.tile, type, 0, 5, null)
        delay(5)
        spawnObject(stump, player.tile, type, 0, 5, null)
    }
}
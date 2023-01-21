import org.rsmod.game.pathfinder.PathFinder
import org.rsmod.game.pathfinder.flag.CollisionFlag
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendContainerItems
import world.gregs.voidps.engine.client.ui.event.Command
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.ui.sendAnimation
import world.gregs.voidps.engine.client.ui.sendText
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.mode.Movement
import world.gregs.voidps.engine.entity.character.player.*
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.entity.obj.spawnObject
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.CollisionFlags
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.path.algorithm.Dijkstra
import world.gregs.voidps.engine.path.strat.NodeTargetStrategy
import world.gregs.voidps.engine.path.traverse.EdgeTraversal
import world.gregs.voidps.engine.timer.Job
import world.gregs.voidps.engine.timer.timer
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.engine.utility.toSentenceCase
import world.gregs.voidps.network.encode.npcDialogueHead
import world.gregs.voidps.network.encode.playerDialogueHead
import world.gregs.voidps.world.interact.dialogue.sendLines
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.entity.gfx.areaGraphic
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis

val collisions: Collisions by inject()


on<Command>({ prefix == "test" }) { player: Player ->
    val pf = PathFinder(flags = collisions, useRouteBlockerFlags = true)
    val start = Tile(3270, 3331, 0)
    val timeShort = measureTimeMillis {
        repeat(100_000) {
            pf.findPath(0, start.x, start.y, 3280, 3321)
        }
    }

    val timeMedium = measureTimeMillis {
        repeat(10_000) {
            pf.findPath(0, start.x, start.y, 3287, 3306)
        }
    }

    val timeLong = measureTimeMillis {
        repeat(1_000) {
            pf.findPath(0, start.x, start.y, 3270, 3268)
        }
    }

    val timeInvalid = measureTimeMillis {
        repeat(1_000) {
            pf.findPath(0, start.x, start.y, 3271, 3235)
        }
    }
    /*val bfs = BreadthFirstSearch(object : DefaultPool<BreadthFirstSearchFrontier>(1) {
        override fun produceInstance() = BreadthFirstSearchFrontier()
    }, get())
    val start = Tile(3270, 3331, 0)
    val collisions = LandCollision(get())
    val traversal = SmallTraversal
    val timeShort = measureTimeMillis {
        val strategy = SingleTileTargetStrategy(Tile(3280, 3321, 0))
        val path = Path(strategy)
        repeat(100_000) {
            bfs.find(start, Size.ONE, path, traversal, collisions)
        }
    }

    val timeMedium = measureTimeMillis {
        val strategy = SingleTileTargetStrategy(Tile(3287, 3306, 0))
        val path = Path(strategy)
        repeat(10_000) {
            bfs.find(start, Size.ONE, path, traversal, collisions)
        }
    }

    val timeLong = measureTimeMillis {
        val strategy = SingleTileTargetStrategy(Tile(3270, 3268, 0))
        val path = Path(strategy)
        repeat(1_000) {
            bfs.find(start, Size.ONE, path, traversal, collisions)
        }
    }

    val timeInvalid = measureTimeMillis {
        val strategy = SingleTileTargetStrategy(Tile(3271, 3235, 0))
        val path = Path(strategy)
        repeat(1_000) {
            bfs.find(start, Size.ONE, path, traversal, collisions)
        }
    }*/

    println("Durations: ")
    println("Short path: ${timeShort / 1000.0}s")
    println("Medium path: ${timeMedium}ms")
    println("Long path: ${timeLong}ms")
    println("Invalid path: ${timeInvalid}ms")
}

on<Command>({ prefix == "rights" }) { player: Player ->
    val right = content.split(" ").last()
    val rights = PlayerRights.valueOf(right.toSentenceCase())
    val username = content.removeSuffix(" $right")
    val target = get<Players>().get(username)
    if (target == null) {
        player.message("Unable to find player '$username'.")
    } else {
        target.rights = rights
        player.message("${player.name} rights set to $rights.")
    }
}

on<Command>({ prefix == "expr" }) { player: Player ->
    val id = content.toIntOrNull()
    if (id != null) {
        val npc = id < 1000
        if (player.open("dialogue_${if (npc) "npc_" else ""}chat1")) {
            if (npc) {
                player.client?.npcDialogueHead(241, 15794178, 2176)
            } else {
                player.client?.playerDialogueHead(64, 4194306)
            }
            player.interfaces.sendAnimation("dialogue_${if (npc) "npc_" else ""}chat1", "head", id)
            player.interfaces.sendText("dialogue_${if (npc) "npc_" else ""}chat1", "title", "title")
            player.interfaces.sendLines("dialogue_${if (npc) "npc_" else ""}chat1", listOf(content))
        }
    } else {
        npc("1902", content, content)
    }
}

on<Command>({ prefix == "showcol" }) { player: Player ->
    val area = player.tile.toCuboid(10)
    val collisions: Collisions = get()
    for (tile in area) {
        if (collisions.get(tile.x, tile.y, tile.plane) != 0) {
            areaGraphic("2000", tile)
        }
    }
}

on<Command>({ prefix == "path" }) { player: Player ->
    player.toggle("show_path")
}

on<EffectStart>({ effect == "show_path" }) { player: Player ->
    player["show_path_job"] = player.timer(1, loop = true) {
        var tile = player.tile
        val movement = player.mode as? Movement ?: return@timer
        for (step in movement.steps) {
            tile = tile.add(step)
            areaGraphic("2000", tile)
        }
    }
}

on<EffectStop>({ effect == "show_path" }) { player: Player ->
    player.remove<Job>("show_path_job")?.cancel()
}

on<Command>({ prefix == "col" }) { player: Player ->
    val collisions: Collisions = get()
    println("Can move north? ${collisions[player.tile.x, player.tile.y, player.tile.plane] and (CollisionFlag.BLOCK_NORTH or CollisionFlag.BLOCK_NORTH_ROUTE_BLOCKER) == 0}")
    println("Can move north? ${collisions[player.tile.x, player.tile.y, player.tile.plane] and CollisionFlag.BLOCK_NORTH == 0}")
    println("Can move north? ${collisions[player.tile.x, player.tile.y, player.tile.plane] and CollisionFlag.WALL_NORTH == 0}")
    println("Can move north? ${collisions[player.tile.x, player.tile.y, player.tile.plane] and CollisionFlag.BLOCK_NORTH_ROUTE_BLOCKER == 0}")
    println(collisions[player.tile.x, player.tile.y - 1, player.tile.plane])
    println(collisions.get(3281, 3327, 0))
    println(player.tile.minus(y = 1))

    println(CollisionFlag.BLOCK_NORTH or CollisionFlag.BLOCK_NORTH_ROUTE_BLOCKER)
    println(CollisionFlag.BLOCK_NORTH)
    println(CollisionFlag.BLOCK_NORTH_ROUTE_BLOCKER)
    println(CollisionFlags.ROUTE_NORTH.bit)
//
//    val pf = SmartPathFinder(flags = collisions.data, useRouteBlockerFlags = false)
//    println(pf.findPath(3205, 3220, 3205, 3223, 2))
}

operator fun Array<IntArray?>.get(baseX: Int, baseY: Int, localX: Int, localY: Int, z: Int): Int {
    val x = baseX + localX
    val y = baseY + localY
    val zone = this[getZoneIndex(x, y, z)] ?: return 0
    return zone[getIndexInZone(x, y)]
}

fun getZoneIndex(x: Int, y: Int, z: Int): Int {
    return (x shr 3) or ((y shr 3) shl 11) or (z shl 22)
}

fun getIndexInZone(x: Int, y: Int): Int {
    return (x and 0x7) or ((y and 0x7) shl 3)
}

on<Command>({ prefix == "walkToBank" }) { player: Player ->
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

on<Command>({ prefix == "sendItems" }) { player: Player ->
    val array = IntArray(28 * 2)
    array[0] = 995
    array[28] = 1
    player.sendContainerItems(90, 28, array, false)
    val ags = IntArray(28 * 2)
    ags[0] = 11694
    ags[28] = 1
    player.sendContainerItems(90, 28, ags, true)
}

on<Command>({ prefix == "obj" }) { player: Player ->
    if (content.isNotBlank()) {
        val parts = content.split(" ")
        val id = parts.getOrNull(0)
        if (id != null) {
            val rotation = parts.getOrNull(1)?.toIntOrNull() ?: 0
            spawnObject(id, player.tile.addY(1), 0, rotation, 10, null)
            spawnObject(id, player.tile.addY(1), 10, rotation, 10, null)
            spawnObject(id, player.tile.addY(1), 22, rotation, 10, null)
        }
    } else {
        get<Objects>()[player.tile].forEach {
            println(it.def.id)
        }
    }
}

on<Command>({ prefix == "tree" }) { player: Player ->
    val parts = content.split(" ")
    val tree = parts[0]
    val stump = parts[1]
    val type = parts.getOrNull(2)?.toIntOrNull() ?: 10
    player.action {
        spawnObject(tree, player.tile, type, 0, 5, null)
        pause(5)
        spawnObject(stump, player.tile, type, 0, 5, null)
    }
}
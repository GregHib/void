package world.gregs.voidps.world.command.debug

import org.rsmod.game.pathfinder.PathFinder
import org.rsmod.game.pathfinder.flag.CollisionFlag
import world.gregs.voidps.bot.path.Dijkstra
import world.gregs.voidps.bot.path.EdgeTraversal
import world.gregs.voidps.bot.path.NodeTargetStrategy
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.client.*
import world.gregs.voidps.engine.client.ui.*
import world.gregs.voidps.engine.client.ui.chat.toSentenceCase
import world.gregs.voidps.engine.client.ui.event.Command
import world.gregs.voidps.engine.client.variable.PlayerVariables
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.player.*
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.entity.obj.spawnObject
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.chunk.DynamicChunks
import world.gregs.voidps.engine.map.collision.CollisionFlags
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.instance.InstancePool
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.engine.suspend.pause
import world.gregs.voidps.engine.timer.TimerQueue
import world.gregs.voidps.engine.timer.TimerTick
import world.gregs.voidps.network.encode.clearCamera
import world.gregs.voidps.network.encode.npcDialogueHead
import world.gregs.voidps.network.encode.playerDialogueHead
import world.gregs.voidps.world.interact.dialogue.Furious
import world.gregs.voidps.world.interact.dialogue.Talk
import world.gregs.voidps.world.interact.dialogue.Unsure
import world.gregs.voidps.world.interact.dialogue.sendLines
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.entity.effect.clearTransform
import world.gregs.voidps.world.interact.entity.effect.transform
import world.gregs.voidps.world.interact.entity.gfx.areaGraphic
import world.gregs.voidps.world.interact.entity.sound.playSound
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis

val collisions: Collisions by inject()
val instances: InstancePool by inject()
val chunks: DynamicChunks by inject()

/**
 *  TODO add padding to instances
 *      add converting from copied coords to new coords
 */
on<Command>({ prefix == "test" }) { player: Player ->
    player.strongQueue("test") {
//        player.start("movement_delay", -1)
        player.open("fade_out")
        player.minimap(Minimap.HideMap)
        pause(2)
        // TODO hide chatbox
        // TODO hide most tabs

        val instance = instances.obtain()
        chunks.copy(Region(12852), instance)
        pause(1)
        player.tele(instance.tile.add(25, 43))
        pause(2)
        player.transform("wally")
        player.clearCamera()
        var offset = instance.tile.add(27, 41) // 3227, 3369
        player.moveCamera(offset.x, offset.y, 300)
        offset = instance.tile.add(29, 39) // 3229, 3367
        player.turnCamera(offset.x, offset.y, 250)
        player.shakeCamera(type = 1, intensity = 0, movement = 10, speed = 10, cycle = 0)
        player.shakeCamera(type = 3, intensity = 0, movement = 90, speed = 1, cycle = 0)
        player.playSound("rumbling") // TODO
        pause(1)
        player.close("fade_out")
        npc<Talk>("gypsy_aris", """
            Wally managed to arrive at the stone circle just as
            Delrith was summoned by a cult of chaos druids...
        """)
        npc<Furious>("wally", "Die, foul demon!", clickToContinue = false)
        player.face(Direction.NORTH)// TODO check 1023
        player.clearCamera()
        offset = instance.tile.add(27, 39) // 3227, 3367
        player.turnCamera(offset.x, offset.y, height = 200, constantSpeed = 2, variableSpeed = 10)
        player.turnCamera(offset.x, offset.y, height = 100, constantSpeed = 1, variableSpeed = 10)
        player.shakeCamera(type = 3, intensity = 0, movement = 0, speed = 0, cycle = 0)
        player.playSound("rumbling")
        offset = instance.tile.add(25, 35) // 3225, 3363
        player.tele(offset, clearInterfaces = false)
        offset = instance.tile.add(27, 39) // 3227, 3367
        pause(2)
        player.start("no_clip", 2)
        player.walkTo(offset)
        pause(2)
        player.face(Direction.NORTH)
        player.setAnimation("wally_demon_slay")
        player.playSound("ds_wally_sword", delay = 10) // TODO
        pause(4)
        player.closeDialogue()
        npc<Unsure>("wally", "Now, what was that incantation again?")

//
//
//        pause(5)
//
        player.stop("movement_delay")
        player.clearMinimap()
        player.clearCamera()
        player.clearTransform()
    }
}

on<Command>({ prefix == "reset_cam" }) { player: Player ->
    player.client?.clearCamera()
}

on<Command>({ prefix == "move_to" }) { player: Player ->
    val test = content.split(" ")
    val viewport = player.viewport!!
    val result = viewport.lastLoadChunk.safeMinus(viewport.chunkRadius, viewport.chunkRadius)
    val local = Tile(test[0].toInt(), test[1].toInt()).minus(result.tile)
    println(local)
    player.moveCamera(local.x, local.y, test[2].toInt(), test[3].toInt(), test[4].toInt())
}

on<Command>({ prefix == "look_at" }) { player: Player ->
    val test = content.split(" ")
    val viewport = player.viewport!!
    val result = viewport.lastLoadChunk.safeMinus(viewport.chunkRadius, viewport.chunkRadius)
    val local = Tile(test[0].toInt(), test[1].toInt()).minus(result.tile)
    println(local)
    player.turnCamera(local.x, local.y, test[2].toInt(), test[3].toInt(), test[4].toInt())
}

on<Command>({ prefix == "shake" }) { player: Player ->
    val test = content.split(" ")
    player.shakeCamera(test[0].toInt(), test[1].toInt(), test[2].toInt(), test[3].toInt(), test[4].toInt())
}

on<Command>({ prefix == "timers" }) { player: Player ->
    player.message("=== Timers ===", ChatType.Console)
    for (timer in player.timers.queue) {
        player.message("${timer.name}: ${timer.nextTick - GameLoop.tick}", ChatType.Console)
    }
    player.message("=== Soft Timers ===", ChatType.Console)
    for (timer in (player.softTimers as TimerQueue).queue) {
        player.message("${timer.name}: ${timer.nextTick - GameLoop.tick}", ChatType.Console)
    }
}

on<Command>({ prefix == "variables" }) { player: Player ->
    player.message("=== Variables ===", ChatType.Console)
    for ((variable, value) in (player.variables as PlayerVariables).temp) {
        player.message("$variable: $value", ChatType.Console)
    }
    player.message("=== Persistent Variables ===", ChatType.Console)
    for ((variable, value) in player.variables.data) {
        player.message("$variable: $value", ChatType.Console)
    }
}

on<Command>({ prefix == "pf_bench" }) { player: Player ->
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
        if (collisions[tile.x, tile.y, tile.plane] != 0) {
            areaGraphic("2000", tile)
        }
    }
}

on<Command>({ prefix == "path" }) { player: Player ->
    player.softTimers.toggle("show_path")
}

on<TimerTick>({ timer == "show_path" }) { player: Player ->
    var tile = player.tile
    for (step in player.steps) {
        tile = tile.add(step)
        areaGraphic("2000", tile)
    }
}

on<Command>({ prefix == "col" }) { player: Player ->
    val collisions: Collisions = get()
    println("Can move north? ${collisions[player.tile.x, player.tile.y, player.tile.plane] and (CollisionFlag.BLOCK_NORTH or CollisionFlag.BLOCK_NORTH_ROUTE_BLOCKER) == 0}")
    println("Can move north? ${collisions[player.tile.x, player.tile.y, player.tile.plane] and CollisionFlag.BLOCK_NORTH == 0}")
    println("Can move north? ${collisions[player.tile.x, player.tile.y, player.tile.plane] and CollisionFlag.WALL_NORTH == 0}")
    println("Can move north? ${collisions[player.tile.x, player.tile.y, player.tile.plane] and CollisionFlag.BLOCK_NORTH_ROUTE_BLOCKER == 0}")
    println(collisions[player.tile.x, player.tile.y - 1, player.tile.plane])
    println(collisions[3281, 3327, 0])
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
    spawnObject(tree, player.tile, type, 0, 5, null)
    pause(5)
    spawnObject(stump, player.tile, type, 0, 5, null)
}
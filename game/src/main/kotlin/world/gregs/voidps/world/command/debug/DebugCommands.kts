import kotlinx.coroutines.Job
import world.gregs.voidps.engine.action.Scheduler
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendContainerItems
import world.gregs.voidps.engine.client.ui.dialogue.dialogue
import world.gregs.voidps.engine.client.ui.event.Command
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.ui.sendAnimation
import world.gregs.voidps.engine.client.ui.sendText
import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerRights
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.rights
import world.gregs.voidps.engine.entity.character.update.visual.player.name
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.entity.obj.spawnObject
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.get
import world.gregs.voidps.engine.map.collision.strategy.LandCollision
import world.gregs.voidps.engine.map.collision.strategy.RoofCollision
import world.gregs.voidps.engine.path.algorithm.Dijkstra
import world.gregs.voidps.engine.path.strat.NodeTargetStrategy
import world.gregs.voidps.engine.path.traverse.EdgeTraversal
import world.gregs.voidps.engine.utility.capitalise
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.network.encode.npcDialogueHead
import world.gregs.voidps.network.encode.playerDialogueHead
import world.gregs.voidps.world.interact.dialogue.sendLines
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.entity.gfx.areaGraphic
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.system.measureNanoTime


on<Command>({ prefix == "test" }) { player: Player ->
}

on<Command>({ prefix == "rights" }) { player: Player ->
    val right = content.split(" ").last()
    val rights = PlayerRights.valueOf(right.capitalise())
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
    player.dialogue {
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
                player.interfaces.sendText("dialogue_${if (npc) "npc_" else ""}chat1", "title", title)
                player.interfaces.sendLines("dialogue_${if (npc) "npc_" else ""}chat1", listOf(content))
                await<Unit>("chat")
            }
        } else {
            npc("1902", "evil_laugh_old", content)
        }
    }
}

on<Command>({ prefix == "showcol" }) { player: Player ->
    val area = player.tile.toCuboid(10)
    val collisions: Collisions = get()
    val col = RoofCollision(collisions, LandCollision(collisions))
    for (tile in area) {
        if (col.free(tile, Direction.NONE)) {
            areaGraphic("2000", tile)
        }
    }
}

on<Command>({ prefix == "path" }) { player: Player ->
    player.toggle("show_path")
}

on<EffectStart>({ effect == "show_path" }) { player: Player ->
    player["show_path_job"] = delay(player, 1, loop = true) {
        var tile = player.tile
        for (step in player.movement.path.steps.toList()) {
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
    println(collisions[player.tile.x, player.tile.y, player.tile.plane])
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
    val scheduler: Scheduler = get()
    player.action {
        var first = true
        while (player.movement.waypoints.isNotEmpty()) {
            val next = player.movement.waypoints.poll()
            suspendCoroutine<Unit> { cont ->
                val tile = if (first && !player.tile.within(next.end as Tile, 20)) {
                    next.start
                } else {
                    next.end
                } as Tile
                first = false
                scheduler.sync {
                    player.walkTo(tile, cancelAction = true) {
                        cont.resume(Unit)
                    }
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
        delay(5)
        spawnObject(stump, player.tile, type, 0, 5, null)
    }
}
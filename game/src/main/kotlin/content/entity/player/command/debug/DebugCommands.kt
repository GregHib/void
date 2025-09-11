package content.entity.player.command.debug

import content.bot.interact.path.Dijkstra
import content.bot.interact.path.EdgeTraversal
import content.bot.interact.path.NodeTargetStrategy
import content.entity.gfx.areaGfx
import content.entity.player.dialogue.sendLines
import content.entity.player.dialogue.type.npc
import content.quest.questJournal
import org.rsmod.game.pathfinder.PathFinder
import org.rsmod.game.pathfinder.StepValidator
import org.rsmod.game.pathfinder.collision.CollisionStrategies
import org.rsmod.game.pathfinder.flag.CollisionFlag
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.client.*
import world.gregs.voidps.engine.client.ui.chat.Colours
import world.gregs.voidps.engine.client.ui.chat.toTag
import world.gregs.voidps.engine.client.ui.event.Command
import world.gregs.voidps.engine.client.ui.event.adminCommand
import world.gregs.voidps.engine.client.ui.event.modCommand
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.PlayerVariables
import world.gregs.voidps.engine.data.definition.PatrolDefinitions
import world.gregs.voidps.engine.entity.character.mode.Patrol
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.isAdmin
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.collision.CollisionFlags
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.timer.TimerQueue
import world.gregs.voidps.engine.timer.timerTick
import world.gregs.voidps.network.login.protocol.encode.*
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis

@Script
class DebugCommands {

    val collisions: Collisions by inject()
    val objects: GameObjects by inject()
    val npcs: NPCs by inject()

    init {
        modCommand("test") {
            player.sendInterfaceItemUpdate(645, listOf(Triple(0, 995, 100)), false)
            //    player.interfaces.sendItem("exchange_item_sets")
        }

        modCommand("commands [list]") {
            val commands = if (player.isAdmin()) Command.adminCommands else Command.modCommands
            val list = listOf(
                "Commands list with descriptions and usage instructions in the format:",
                "${Colours.BLUE.toTag()}command_name (required-variable) [optional-variable]</col>",
                "command description",
                "",
            )
            player.questJournal("Commands List", list + commands)
        }

        modCommand("help (command-name)", "gives more information about a command") {
            // TODO find a way to automate this.
            when (content) {
                "reload" -> {
                    player.message("Reload configuration files for the game server.", ChatType.Console)
                    player.message("config-names:", ChatType.Console)
                    player.message("books, stairs, songs, objects, nav graph, npcs, areas, object defs, emotes, anims", ChatType.Console)
                    player.message("invs, graphics, npc defs, item-on-item, sounds, quests, midi, vars, music, interfaces", ChatType.Console)
                    player.message("spells, patrols, prayers, drops, client scripts, settings", ChatType.Console)
                }
                "unlock" -> {
                    player.message("Unlock content of a specific type.", ChatType.Console)
                    player.message("activity-type:", ChatType.Console)
                    player.message("music, tasks, emotes, quests, or blank to unlock all.", ChatType.Console)
                }
                "find" -> {
                    player.message("Find the string or integer id of a piece of content.", ChatType.Console)
                    player.message("content-types:", ChatType.Console)
                    player.message("items, objects, npcs, commands", ChatType.Console)
                }
                else -> {
                    player.message("No help info found for command '$content'.", ChatType.Console)
                    player.message("Enter 'commands' for full list of commands.", ChatType.Console)
                }
            }
        }

        Command.adminCommands.add("${Colours.PURPLE.toTag()}====== Testing Commands ======</col>")

        Command.adminCommands.add("")

        adminCommand("patrol (patrol-id)", "walk along a patrol route") {
            val patrol = get<PatrolDefinitions>().get(content)
            player.tele(patrol.waypoints.first().first)
            player.mode = Patrol(player, patrol.waypoints)
        }

        modCommand("reset_cam", "reset camera to normal") {
            player.client?.clearCamera()
        }

        adminCommand("move_to (x) (y) (height) (c-speed) (v-speed)", "move camera to look at coordinates") {
            val test = content.split(" ")
            val viewport = player.viewport!!
            val result = viewport.lastLoadZone.safeMinus(viewport.zoneRadius, viewport.zoneRadius)
            val local = Tile(test[0].toInt(), test[1].toInt()).minus(result.tile)
            println(local)
            player.moveCamera(local, test[2].toInt(), test[3].toInt(), test[4].toInt())
        }

        adminCommand("look_at (x) (y) (height) (c-speed) (v-speed)", "turn camera to look at coordinates") {
            val test = content.split(" ")
            val viewport = player.viewport!!
            val result = viewport.lastLoadZone.safeMinus(viewport.zoneRadius, viewport.zoneRadius)
            val local = Tile(test[0].toInt(), test[1].toInt()).minus(result.tile)
            println(local)
            player.turnCamera(local, test[2].toInt(), test[3].toInt(), test[4].toInt())
        }

        adminCommand("shake (intensity) (type) (cycle) (movement) (speed)", "shake camera") {
            val test = content.split(" ")
            player.shakeCamera(test[0].toInt(), test[1].toInt(), test[2].toInt(), test[3].toInt(), test[4].toInt())
        }

        modCommand("timers", "list all players active timers") {
            player.message("=== Timers ===", ChatType.Console)
            for (timer in player.timers.queue) {
                player.message("${timer.name}: ${timer.nextTick - GameLoop.tick}", ChatType.Console)
            }
            player.message("=== Soft Timers ===", ChatType.Console)
            for (timer in (player.softTimers as TimerQueue).queue) {
                player.message("${timer.name}: ${timer.nextTick - GameLoop.tick}", ChatType.Console)
            }
        }

        modCommand("variables", "list all players variables", listOf("vars")) {
            player.message("=== Variables ===", ChatType.Console)
            for ((variable, value) in (player.variables as PlayerVariables).temp.toSortedMap()) {
                if (content.isNotBlank() && !variable.contains(content, ignoreCase = true)) {
                    continue
                }
                player.message("$variable: $value", ChatType.Console)
            }
            player.message("=== Persistent Variables ===", ChatType.Console)
            for ((variable, value) in player.variables.data.toSortedMap()) {
                if (content.isNotBlank() && !variable.contains(content, ignoreCase = true)) {
                    continue
                }
                player.message("$variable: $value", ChatType.Console)
            }
        }

        adminCommand("pf_bench") {
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

        adminCommand("expr (animation-id)", "display dialogue head with an animation expression by id") {
            val id = content.toIntOrNull()
            if (id != null) {
                val npc = id < 1000
                if (player.open("dialogue_${if (npc) "npc_" else ""}chat1")) {
                    if (npc) {
                        player.client?.npcDialogueHead(15794178, 2176)
                    } else {
                        player.client?.playerDialogueHead(4194306)
                    }
                    player.interfaces.sendAnimation("dialogue_${if (npc) "npc_" else ""}chat1", "head", id)
                    player.interfaces.sendText("dialogue_${if (npc) "npc_" else ""}chat1", "title", "title")
                    player.interfaces.sendLines("dialogue_${if (npc) "npc_" else ""}chat1", listOf(content))
                }
            } else {
                npc("1902", content, content)
            }
        }

        adminCommand("showcol", "show nearby collision") {
            val area = player.tile.toCuboid(10)
            val collisions: Collisions = get()
            val steps: StepValidator = get()
            val strategy = CollisionStrategies.Normal
            next@ for (tile in area) {
                val size = 3
                for (i in 1 until size) {
                    if (!steps.canTravel(tile.level, tile.x - i, tile.y, 1, 0, size)) {
                        continue@next
                    }
                    if (!steps.canTravel(tile.level, tile.x, tile.y - i, 0, 1, size)) {
                        continue@next
                    }
                    if (!steps.canTravel(tile.level, tile.x + i, tile.y, -1, 0, size)) {
                        continue@next
                    }
                    if (!steps.canTravel(tile.level, tile.x, tile.y + i, 0, -1, size)) {
                        continue@next
                    }
                }
                areaGfx("2000", tile)
            }
        }

        adminCommand("path", "show calculated walk paths") {
            player.softTimers.toggle("show_path")
        }

        timerTick("show_path") { player ->
            var tile = player.tile
            for (step in player.steps) {
                tile = tile.add(step)
                areaGfx("2000", tile)
            }
        }

        adminCommand("col") {
            val collisions: Collisions = get()
            println("Can move north? ${collisions[player.tile.x, player.tile.y, player.tile.level] and (CollisionFlag.BLOCK_NORTH or CollisionFlag.BLOCK_NORTH_ROUTE_BLOCKER) == 0}")
            println("Can move north? ${collisions[player.tile.x, player.tile.y, player.tile.level] and CollisionFlag.BLOCK_NORTH == 0}")
            println("Can move north? ${collisions[player.tile.x, player.tile.y, player.tile.level] and CollisionFlag.WALL_NORTH == 0}")
            println("Can move north? ${collisions[player.tile.x, player.tile.y, player.tile.level] and CollisionFlag.BLOCK_NORTH_ROUTE_BLOCKER == 0}")
            println(collisions[player.tile.x, player.tile.y, player.tile.level])
            println(player.tile.minus(y = 1))

            println(CollisionFlag.BLOCK_NORTH or CollisionFlag.BLOCK_NORTH_ROUTE_BLOCKER)
            println(CollisionFlag.BLOCK_NORTH)
            println(CollisionFlag.BLOCK_NORTH_ROUTE_BLOCKER)
            println(CollisionFlags.ROUTE_NORTH.bit)
            //
            //    val pf = SmartPathFinder(flags = collisions.data, useRouteBlockerFlags = false)
            //    println(pf.findPath(3205, 3220, 3205, 3223, 2))
        }

        adminCommand("walkToBank") {
            val east = Tile(3179, 3433).toCuboid(15, 14)
            val west = Tile(3250, 3417).toCuboid(7, 8)
            val dijkstra: Dijkstra = get()
            val strategy = object : NodeTargetStrategy() {
                override fun reached(node: Any): Boolean = if (node is Tile) east.contains(node) || west.contains(node) else false
            }
            println(
                "Path took ${
                    measureNanoTime {
                        dijkstra.find(player, strategy, EdgeTraversal())
                    }
                }ns",
            )
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

        adminCommand("sendItems") {
            val array = IntArray(28 * 2)
            array[0] = 995
            array[28] = 1
            player.sendInventoryItems(90, 28, array, false)
            val ags = IntArray(28 * 2)
            ags[0] = 11694
            ags[28] = 1
            player.sendInventoryItems(90, 28, ags, true)
        }

        adminCommand("obj (object-id) [object-shape] [object-rotation] [ticks]", "spawn an object") {
            val parts = content.split(" ")
            val id = parts[0]
            val shape = parts.getOrNull(1)?.toIntOrNull() ?: 10
            val rotation = parts.getOrNull(2)?.toIntOrNull() ?: 0
            val ticks = parts.getOrNull(3)?.toIntOrNull() ?: -1
            objects.add(id, player.tile, shape, rotation, ticks)
        }

        adminCommand("under [type]", "display entity types underneath the player") {
            val type = content
            if (type == "" || type == "obj" || type == "objs" || type == "objects") {
                val objs = get<GameObjects>()[player.tile]
                if (objs.isNotEmpty()) {
                    player.message("--- Objects ---", ChatType.Console)
                    for (obj in objs) {
                        player.message(obj.toString(), ChatType.Console)
                    }
                }
            }
            if (type == "" || type == "players") {
                val players = get<Players>()[player.tile].filterNot { it == player }
                if (players.isNotEmpty()) {
                    player.message("--- Players ---", ChatType.Console)
                    for (other in players) {
                        player.message(other.toString(), ChatType.Console)
                    }
                }
            }
            if (type == "" || type == "npcs") {
                val npcs = get<NPCs>()[player.tile]
                if (npcs.isNotEmpty()) {
                    player.message("--- NPCs ---", ChatType.Console)
                    for (npc in npcs) {
                        player.message(npc.toString(), ChatType.Console)
                    }
                }
            }
            if (type == "" || type == "items" || type == "floor items" || type == "floor_items") {
                val items = get<FloorItems>()[player.tile]
                if (items.isNotEmpty()) {
                    player.message("--- Floor Items ---", ChatType.Console)
                    for (item in items) {
                        player.message(item.toString(), ChatType.Console)
                    }
                }
            }
        }

        adminCommand("tree") {
            val parts = content.split(" ")
            val tree = parts[0]
            val stump = parts[1]
            val type = parts.getOrNull(2)?.toIntOrNull() ?: 10
            objects.add(tree, player.tile, type, 0, 5)
            pause(5)
            objects.add(stump, player.tile, type, 0, 5)
        }
    }

    operator fun Array<IntArray?>.get(baseX: Int, baseY: Int, localX: Int, localY: Int, z: Int): Int {
        val x = baseX + localX
        val y = baseY + localY
        val zone = this[Zone.tileIndex(x, y, z)] ?: return 0
        return zone[Tile.index(x, y)]
    }
}

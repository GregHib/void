package content.entity.player.command

import content.bot.Bot
import content.bot.BotManager
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.action.BotGoTo
import content.bot.behaviour.navigation.NavigationGraph
import content.bot.behaviour.setup.Resolver
import content.bot.bot
import content.bot.isBot
import content.entity.gfx.areaGfx
import org.rsmod.game.pathfinder.PathFinder
import org.rsmod.game.pathfinder.StepValidator
import org.rsmod.game.pathfinder.collision.CollisionStrategies
import org.rsmod.game.pathfinder.flag.CollisionFlag
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.PatrolDefinitions
import world.gregs.voidps.engine.entity.character.mode.Patrol
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.collision.CollisionFlags
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.type.Tile
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis

class PathFindingCommands(val patrols: PatrolDefinitions) : Script {

    init {
        adminCommand("patrol", stringArg("patrol-id", autofill = patrols.definitions.keys), desc = "Walk along a patrol route") { args ->
            val patrol = patrols.get(args[0])
            tele(patrol.waypoints.first().first)
            mode = Patrol(this, patrol.waypoints, loop = false)
        }

        adminCommand("show_patrol", stringArg("patrol-id", autofill = patrols.definitions.keys), desc = "Display a patrol route") { args ->
            val patrol = patrols.get(args[0])
            for ((tile, delay) in patrol.waypoints) {
                areaGfx("2000", tile)
            }
        }

        adminCommand("pf_bench") {
            val pf = PathFinder(flags = Collisions.map, useRouteBlockerFlags = true)
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

        adminCommand("show_col", desc = "Show nearby collision") {
            val area = tile.toCuboid(10)
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

        adminCommand("path", desc = "Show calculated walk paths") {
            softTimers.toggle("show_path")
        }

        adminCommand("col") {
            val collisions: Collisions = get()
            println("Can move north? ${collisions[tile.x, tile.y, tile.level] and (CollisionFlag.BLOCK_NORTH or CollisionFlag.BLOCK_NORTH_ROUTE_BLOCKER) == 0}")
            println("Can move north? ${collisions[tile.x, tile.y, tile.level] and CollisionFlag.BLOCK_NORTH == 0}")
            println("Can move north? ${collisions[tile.x, tile.y, tile.level] and CollisionFlag.WALL_NORTH == 0}")
            println("Can move north? ${collisions[tile.x, tile.y, tile.level] and CollisionFlag.BLOCK_NORTH_ROUTE_BLOCKER == 0}")
            println(collisions[tile.x, tile.y, tile.level])
            println(tile.minus(y = 1))

            println(CollisionFlag.BLOCK_NORTH or CollisionFlag.BLOCK_NORTH_ROUTE_BLOCKER)
            println(CollisionFlag.BLOCK_NORTH)
            println(CollisionFlag.BLOCK_NORTH_ROUTE_BLOCKER)
            println(CollisionFlags.ROUTE_NORTH.bit)
            //
            //    val pf = SmartPathFinder(flags = collisions.data, useRouteBlockerFlags = false)
            //    println(pf.findPath(3205, 3220, 3205, 3223, 2))
        }

        adminCommand("go_to", stringArg("area-id", autofill = Areas.getAll().map { it.stringId }.toSet(), optional = true), desc = "Bot walk to a location") { args ->
            val area = args.getOrNull(0) ?: "varrock_teleport"
            if (!isBot) {
                val manager = get<BotManager>()
                val bot = Bot(this)
                set("bot", bot)
                manager.add(bot)
            }
            bot.queue(BehaviourFrame(Resolver("bot_to_$area", 0, actions = listOf(BotGoTo(area)))))
        }

        adminCommand("walk_to_bank") {
            val graph: NavigationGraph = get()
            val output = mutableListOf<Int>()
            println(
                "Path took ${
                    measureNanoTime {
                        graph.findNearest(this, output, "bank")
                    }
                }ns",
            )
        }

        timerTick("show_path") {
            var tile = tile
            for (step in steps) {
                tile = tile.add(step)
                areaGfx("2000", tile)
            }
            return@timerTick Timer.CONTINUE
        }
    }
}

package content.entity.player.command

import content.bot.interact.path.Dijkstra
import content.bot.interact.path.EdgeTraversal
import content.bot.interact.path.NodeTargetStrategy
import content.entity.gfx.areaGfx
import org.rsmod.game.pathfinder.PathFinder
import org.rsmod.game.pathfinder.StepValidator
import org.rsmod.game.pathfinder.collision.CollisionStrategies
import org.rsmod.game.pathfinder.flag.CollisionFlag
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.data.definition.PatrolDefinitions
import world.gregs.voidps.engine.entity.character.mode.Patrol
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.collision.CollisionFlags
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.timer.timerTick
import world.gregs.voidps.type.Tile
import kotlin.getValue
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis

@Script
class PathFindingCommands {

    val patrols: PatrolDefinitions by inject()
    val collisions: Collisions by inject()

    init {
        adminCommand("patrol", stringArg("patrol-id", autofill = patrols.definitions.keys), desc = "Walk along a patrol route") { player, args ->
            val patrol = patrols.get(args[0])
            player.tele(patrol.waypoints.first().first)
            player.mode = Patrol(player, patrol.waypoints)
        }

        adminCommand("pf_bench") { player, _ ->
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

        adminCommand("show_col", desc = "Show nearby collision") { player, _ ->
            val area = player.tile.toCuboid(10)
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

        adminCommand("path", desc = "Show calculated walk paths") { player, _ ->
            player.softTimers.toggle("show_path")
        }

        timerTick("show_path") { player ->
            var tile = player.tile
            for (step in player.steps) {
                tile = tile.add(step)
                areaGfx("2000", tile)
            }
        }

        adminCommand("col") { player, _ ->
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

        adminCommand("walk_to_bank") { player, _ ->
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
    }
}

import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.contains
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.set
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.path.algorithm.Dijkstra
import world.gregs.voidps.engine.path.strat.NodeTargetStrategy
import world.gregs.voidps.engine.path.traverse.EdgeTraversal
import world.gregs.voidps.engine.tick.AiTick
import world.gregs.voidps.network.Instruction
import world.gregs.voidps.network.instruct.Command
import world.gregs.voidps.network.instruct.Walk
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.entity.bot.botOptions
import world.gregs.voidps.world.interact.entity.bot.context
import world.gregs.voidps.world.interact.entity.bot.initBot
import world.gregs.voidps.world.interact.entity.bot.isBot
import java.util.*

val dijkstra: Dijkstra by inject()

on<Command>({ prefix == "exec" }) { player: Player ->
    val target = Tile(3229, 3214, 2)
    val strategy = object : NodeTargetStrategy() {
        override fun reached(node: Any): Boolean {
            return node == target
        }
    }
    dijkstra.find(player, strategy, EdgeTraversal())
    val list = LinkedList<Instruction>()
    for (edge in player.movement.waypoints) {
        for (instruction in edge.steps) {
            list.add(instruction)
        }
    }

    var current: Instruction? = null

    fun next(): Boolean {
        if (current is Walk) {
            val walk = current as Walk
            if (player.tile.within(walk.x, walk.y, 1)) {
                return true
            }

        }
        return player.action.type == ActionType.None
    }
    World.events.on<World, AiTick> {
        if (list.isNotEmpty() && next()) {
            current = list.peek()
            player.instructions.tryEmit(list.poll())
        }
    }
}

on<Command>({ prefix == "bot" }) { player: Player ->
    when {
        player.isBot -> player["bot"] = false
        player.contains("context") -> player["bot"] = true
        else -> {
            player.initBot()
            player.events.emit(Registered)
        }
    }
}

on<Command>({ prefix == "eval" }) { player: Player ->
    for (option in player.botOptions) {
        println(option.name)
        for (score in option.getScores(player.context)) {
            println(score)
        }
    }
}
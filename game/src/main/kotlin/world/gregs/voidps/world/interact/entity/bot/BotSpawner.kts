package world.gregs.voidps.world.interact.entity.bot

import kotlinx.coroutines.*
import world.gregs.voidps.ai.*
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.Contexts
import world.gregs.voidps.engine.action.Scheduler
import world.gregs.voidps.engine.action.delay
import world.gregs.voidps.engine.data.PlayerFactory
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.get
import world.gregs.voidps.engine.entity.character.getOrNull
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.login.LoginQueue
import world.gregs.voidps.engine.entity.character.set
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.Area
import world.gregs.voidps.engine.map.area.Rectangle
import world.gregs.voidps.engine.map.equals
import world.gregs.voidps.engine.map.nav.NavigationGraph
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.path.algorithm.Dijkstra
import world.gregs.voidps.engine.path.strat.NodeTargetStrategy
import world.gregs.voidps.engine.path.traverse.EdgeTraversal
import world.gregs.voidps.engine.tick.AiTick
import world.gregs.voidps.network.Instruction
import world.gregs.voidps.network.instruct.Command
import world.gregs.voidps.network.instruct.Walk
import world.gregs.voidps.utility.inject
import java.util.*

val scheduler: Scheduler by inject()
val graph: NavigationGraph by inject()
val dijkstra: Dijkstra by inject()
val decisionMaker = DecisionMaker()

class BotContext(val bot: Player) : Context {
    override var last: Decision<*, *>? = null
}

open class SimpleBotOption<T : Any>(
    val name: String,
    override val targets: BotContext.() -> List<T>,
    override val considerations: Set<BotContext.(T) -> Double>,
    override val momentum: Double = 1.0,
    override val weight: Double = 1.0,
    override val action: BotContext.(T) -> Unit
) : Option<BotContext, T>

val decideTarget = SimpleBotOption(
    name = "choose target",
    targets = { listOf(this) },
    considerations = setOf(
        { bot.movement.waypoints.isEmpty().toDouble() }
    ),
    weight = 0.2,
    action = {
        val target = graph.tiles.keys.firstOrNull { it is Tile && it.equals(3229, 3216, 1) }
        if (target == null) {
            return@SimpleBotOption
        }
        val strategy = object : NodeTargetStrategy() {
            override fun reached(node: Any): Boolean {
                return node == target
            }
        }
        dijkstra.find(bot, strategy, EdgeTraversal())
    }
)

fun Player.goTo(area: Area): PathResult {
    val strategy = object : NodeTargetStrategy() {
        override fun reached(node: Any): Boolean {
            return node is Tile && node in area
        }
    }
    return dijkstra.find(this, strategy, EdgeTraversal())
}

fun hasTarget(bot: Player): Boolean {
    if (bot.steps.isNotEmpty()) {
        return true
    }
    return bot.movement.waypoints.isNotEmpty()
}

fun isNearingStepCompletion(bot: Player): Boolean {
    if (bot.step is Walk) {
        val walk = bot.step as Walk
        if (bot.tile.within(walk.x, walk.y, 1)) {
            return true
        }
    }
    return bot.action.type == ActionType.None
}

val Player.steps: LinkedList<Instruction>
    get() = get("steps", LinkedList())

var Player.step: Instruction?
    get() = getOrNull("step")
    set(value) {
        if (value != null) {
            set("step", value)
        }
    }

val walkToTarget = SimpleBotOption(
    "target walk",
    targets = { listOf(this) },
    considerations = setOf(
        { isNearingStepCompletion(bot).toDouble() },
        { hasTarget(bot).toDouble() }
    ),
    weight = 0.1,
    action = {
        val steps = bot.steps
        if (steps.isEmpty()) {
            val next = bot.movement.waypoints.poll()
            steps.addAll(next.steps)
        }
        if (steps.isNotEmpty()) {
            val step = steps.poll()
            bot.step = step
            bot.instructions.tryEmit(step)
        }
    }
)

val options = setOf(
    walkToTarget
)

val bots = mutableListOf<Player>()

val loginQueue: LoginQueue by inject()
val factory: PlayerFactory by inject()
val players: Players by inject()

on<Command>({ prefix == "bot" }) { player: Player ->
    spawnBots(1)
}

on<Command>({ prefix == "goto" }) { player: Player ->
    val bot = players.indexed.first { it != null && it.name.startsWith("Bot") } ?: return@on
    println(bot.goTo(when (content) {
        "bank" -> lumbridgeCastleBank
        "axes" -> bobsAxeShop
        "trees" -> trees
        else -> return@on
    }))
}

var counter = 0
val varrock = Tile(3212, 3428)
val lumbridge = Rectangle(3221, 3217, 3222, 3220)

val trees = Rectangle(3221, 3244, 3233, 3249)
val bobsAxeShop = Rectangle(3227, 3201, 3233, 3205)
val lumbridgeCastleBank = Rectangle(3207, 3215, 3210, 3222, 2)

fun spawnBots(count: Int) {
    repeat(count) {
        GlobalScope.launch(Contexts.Game) {
            val name = "Bot ${++counter}"
            val index = loginQueue.login(name)!!
            val bot = Player(index = index, tile = lumbridge.random(), name = name)
            factory.initPlayer(bot, index)
            loginQueue.await()
            bot.login()

            bot["context"] = BotContext(bot)
            bot["steps"] = LinkedList<Instruction>()
            scheduler.launch {
                delay(1)
                bot.viewport.loaded = true
                delay(2)
                bot.action.type = ActionType.None
                bots.add(bot)
                bot.movement.running = true
            }
        }
    }

}

on<World, AiTick> {
    runBlocking {
        coroutineScope {
            bots.forEach { bot ->
                if (!bot.movement.target) {
                    launch(Contexts.Updating) {
                        bot.viewport.loaded = true
                        calculateNewAction(bot)
                    }
                }
            }
        }
    }
}

fun calculateNewAction(player: Player) {
    val context: BotContext = player["context"]
    decisionMaker.invoke(context, options)
}
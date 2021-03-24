package world.gregs.voidps.world.interact.entity.bot

import kotlinx.coroutines.*
import world.gregs.voidps.ai.*
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.Contexts
import world.gregs.voidps.engine.action.Scheduler
import world.gregs.voidps.engine.action.delay
import world.gregs.voidps.engine.data.PlayerLoader
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.get
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.Command
import world.gregs.voidps.engine.entity.character.player.login.LoginQueue
import world.gregs.voidps.engine.entity.character.set
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.Rectangle
import world.gregs.voidps.engine.map.area.area
import world.gregs.voidps.engine.map.equals
import world.gregs.voidps.engine.map.nav.NavigationGraph
import world.gregs.voidps.engine.path.algorithm.Dijkstra
import world.gregs.voidps.engine.path.strat.NodeTargetStrategy
import world.gregs.voidps.engine.path.traverse.EdgeTraversal
import world.gregs.voidps.engine.tick.Tick
import world.gregs.voidps.utility.inject

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
val walkToTarget = SimpleBotOption(
    "target walk",
    targets = { listOf(this) },
    considerations = setOf(
        { bot.movement.waypoints.isNotEmpty().toDouble() },
        { (bot.movement.completable != null).toDouble() }
    ),
    weight = 0.1,
    action = {
        val next = bot.movement.waypoints.poll()
        val random = (next.end as Tile).area(1).toList().random()
        bot.walkTo(random) {
            bot.action.type = ActionType.None
        }
    }
)

val options = setOf(
    walkToTarget,
    decideTarget
)

val bots = mutableListOf<Player>()

val loginQueue: LoginQueue by inject()
val loader: PlayerLoader by inject()

on<Command>({ prefix == "bots" }) { player: Player ->
    spawnBots(1)
}

var counter = 0
val varrock = Tile(3212, 3428)
val lumbridge = Rectangle(3221, 3217, 3222, 3220)

fun spawnBots(count: Int) {
    repeat(count) {
        GlobalScope.launch(Contexts.Game) {
            val name = "Bot ${++counter}"
            val index = loginQueue.login(name)!!
            val bot = Player(index = index, tile = lumbridge.random(), name = name)
            loader.initPlayer(bot, index)
            loginQueue.await()
            bot.login()

            bot["context"] = BotContext(bot)
            scheduler.launch {
                delay(1)
                bot.viewport.loaded = true
                delay(2)
                bot.action.type = ActionType.None
                bots.add(bot)
            }
        }
    }

}

on<World, Tick> {
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
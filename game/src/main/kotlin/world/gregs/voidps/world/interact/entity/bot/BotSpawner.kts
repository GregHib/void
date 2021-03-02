package world.gregs.voidps.world.interact.entity.bot

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import world.gregs.voidps.ai.*
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.Contexts
import world.gregs.voidps.engine.action.Scheduler
import world.gregs.voidps.engine.action.delay
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.get
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.login.Login
import world.gregs.voidps.engine.entity.character.player.login.LoginQueue
import world.gregs.voidps.engine.entity.character.player.login.LoginResponse
import world.gregs.voidps.engine.entity.character.player.login.PlayerRegistered
import world.gregs.voidps.engine.entity.character.set
import world.gregs.voidps.engine.entity.character.update.visual.player.tele
import world.gregs.voidps.engine.event.EventBus
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.engine.event.where
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.area
import world.gregs.voidps.engine.map.nav.NavigationGraph
import world.gregs.voidps.engine.path.algorithm.Dijkstra
import world.gregs.voidps.engine.path.strat.NodeTargetStrategy
import world.gregs.voidps.engine.path.traverse.EdgeTraversal
import world.gregs.voidps.engine.tick.Tick
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.command.Command

val bus: EventBus by inject()
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
        val target = graph.tiles.keys.random()
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
        { (bot.movement.completable?.isCompleted ?: true).toDouble() }
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

Command where { prefix == "bots" } then {
    spawnBots(2000)
}
var counter = 0

fun spawnBots(count: Int) {
    repeat(count) { i ->
        val callback = { response: LoginResponse ->
            if (response is LoginResponse.Success) {
                val bot = response.player
                bus.emit(PlayerRegistered(bot))
                bus.emit(Registered(bot))
                bot.start()
                bot.setup()
                bot["context"] = BotContext(bot)
                scheduler.launch {
                    delay(1)
                    bot.tele(3212, 3428, 0)
                    bot.viewport.loaded = true
                    delay(2)
                    bot.action.type = ActionType.None
                    bots.add(bot)
                }
            }
        }
        loginQueue.add(
            Login(
                "Bot ${++counter}",
                callback = callback
            )
        )
    }
}

Tick then {
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
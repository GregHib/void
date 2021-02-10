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
import world.gregs.voidps.engine.tick.Tick
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.command.Command
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis

val bus: EventBus by inject()
val scheduler: Scheduler by inject()
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

val randomWalk = SimpleBotOption(
    "target walk",
    targets = {
        bot.tile.area(15).toList()
    },
    considerations = setOf(
        { tile -> tile.distanceTo(bot.get<Tile>("walkTarget")).toDouble().scale(1.0, 100.0).inverse() }
    ),
    weight = 0.1,
    action = { target ->
        bot.walkTo(target) {
            bot.action.type = ActionType.None
            bot["walkTarget"] = targets.random()
        }
    }
)

val options = setOf(
    randomWalk
)

val targets = setOf(
    Tile(3217, 3415, 0),
    Tile(3204, 3416, 0),
    Tile(3205, 3397, 0),
    Tile(3223, 3399, 0),
    Tile(3203, 3424, 0),
    Tile(3202, 3435, 0),
    Tile(3229, 3438, 0),
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
                bot["walkTarget"] = targets.random()
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
    println("Bot decisions took ${measureTimeMillis {
        runBlocking {
            coroutineScope {
                bots.forEach { bot ->
                    if (bot.movement.target == null) {
                        launch(Contexts.Updating) {
                            bot.viewport.loaded = true
                            calculateNewAction(bot)
                        }
                    }
                }
            }
        }
    }
    }ms")
}

fun calculateNewAction(player: Player) {
    val context: BotContext = player["context"]
    val took = measureNanoTime {
        val decision = decisionMaker.invoke(context, options)
        decision
    }
//    println("Decision made in ${took}ns")
}
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import world.gregs.voidps.bot.isBot
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.Contexts
import world.gregs.voidps.engine.action.Scheduler
import world.gregs.voidps.engine.action.delay
import world.gregs.voidps.engine.client.ConnectionGatekeeper
import world.gregs.voidps.engine.client.ConnectionQueue
import world.gregs.voidps.engine.client.ui.event.Command
import world.gregs.voidps.engine.data.PlayerFactory
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.player.Bot
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventHandlerStore
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.area.Rectangle
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.engine.utility.inject
import kotlin.random.Random

val scheduler: Scheduler by inject()
val bots = mutableListOf<Player>()

val queue: ConnectionQueue by inject()
val gatekeeper: ConnectionGatekeeper by inject()
val factory: PlayerFactory by inject()

on<Command>({ prefix == "bot" }) { player: Player ->
    if (player.isBot) {
        player.clear("bot")
    } else {
        player.initBot()
        if (content.isNotBlank()) {
            player["task"] = content
        }
        player.events.emit(Registered)
    }
}

var counter = 0

on<Command>({ prefix == "bots" }) { _: Player ->
    val count = content.toIntOrNull() ?: 1
    val lumbridge = Rectangle(3221, 3217, 3222, 3220)
    GlobalScope.launch {
        repeat(count) {
            delay(Random.nextInt(1, 10))
            GlobalScope.launch(Contexts.Game) {
                val name = "Bot ${++counter}"
                val index = gatekeeper.connect(name)!!
                val bot = factory.getOrElse(name, index) { Player(index = index, tile = lumbridge.random(), name = name) }
                queue.await()
                if (bot.inventory.isEmpty()) {
                    bot.inventory.add("coins", 10000)
                }
                bot.initBot()
                bot.login()
                scheduler.launch {
                    delay(1)
                    bot.viewport.loaded = true
                    delay(2)
                    bot.action.type = ActionType.None
                    bots.add(bot)
                    bot.running = true
                }
            }
        }
    }
}

fun Player.initBot() {
    val bot = Bot(this)
    get<EventHandlerStore>().populate(Bot::class, bot.botEvents)
    this["bot"] = bot
    val e = mutableListOf<Event>()
    this["events"] = e
    events.all = {
        e.add(it)
    }
}
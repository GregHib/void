import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import world.gregs.voidps.bot.isBot
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.Contexts
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
import world.gregs.voidps.engine.tick.Scheduler
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.engine.utility.inject
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.resume
import kotlin.reflect.KClass

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
    val scheduler: Scheduler = get()
    GlobalScope.launch {
        repeat(count) {
            if (it % 10 == 0) {
                suspendCancellableCoroutine<Unit> { cont ->
                    scheduler.add {
                        cont.resume(Unit)
                    }
                }
            }
            GlobalScope.launch(Contexts.Game) {
                val name = "Bot ${++counter}"
                val index = gatekeeper.connect(name)!!
                val bot = factory.getOrElse(name, index) { Player(index = index, tile = lumbridge.random(), accountName = name) }
                queue.await()
                if (bot.inventory.isEmpty()) {
                    bot.inventory.add("coins", 10000)
                }
                bot.initBot()
                bot.login()
                scheduler.add(1) {
                    bot.viewport.loaded = true
                    scheduler.add(2) {
                        bot.action.type = ActionType.None
                        bots.add(bot)
                        bot.running = true
                    }
                }
            }
        }
    }
}

fun Player.initBot() {
    val bot = Bot(this)
    get<EventHandlerStore>().populate(Bot::class, bot.botEvents)
    this["bot"] = bot
    val e = ConcurrentLinkedQueue<Event>()
    this["events"] = e
    events.all = { event ->
        e.add(event)
        handleSuspensions(bot.player, event)
    }
}

fun handleSuspensions(player: Player, event: Event) {
    val suspensions: MutableMap<KClass<*>, Pair<Event.(Player) -> Boolean, CancellableContinuation<Unit>>> = player.getOrNull("bot_suspensions") ?: return
    val pair = suspensions[event::class] ?: return
    val (condition, continuation) = pair
    if (condition(event, player)) {
        suspensions.remove(event::class)
        continuation.resume(Unit)
    }
}
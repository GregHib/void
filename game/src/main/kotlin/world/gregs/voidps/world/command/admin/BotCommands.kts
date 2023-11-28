package world.gregs.voidps.world.command.admin

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import world.gregs.voidps.bot.Bot
import world.gregs.voidps.bot.StartBot
import world.gregs.voidps.bot.TaskManager
import world.gregs.voidps.bot.isBot
import world.gregs.voidps.engine.Contexts
import world.gregs.voidps.engine.client.ConnectionGatekeeper
import world.gregs.voidps.engine.client.ConnectionQueue
import world.gregs.voidps.engine.client.ui.event.Command
import world.gregs.voidps.engine.data.PlayerAccounts
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.StructDefinitions
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.appearance
import world.gregs.voidps.engine.entity.character.player.sex
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventHandlerStore
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.suspend.pause
import world.gregs.voidps.network.DummyClient
import world.gregs.voidps.network.visual.update.player.BodyColour
import world.gregs.voidps.network.visual.update.player.BodyPart
import world.gregs.voidps.type.area.Rectangle
import world.gregs.voidps.type.random
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.resume
import kotlin.reflect.KClass

val bots = mutableListOf<Player>()

val queue: ConnectionQueue by inject()
val gatekeeper: ConnectionGatekeeper by inject()
val accounts: PlayerAccounts by inject()
val enums: EnumDefinitions by inject()
val structs: StructDefinitions by inject()

on<Command>({ prefix == "bot" }) { player: Player ->
    if (player.isBot) {
        player.clear("bot")
    } else {
        val bot = player.initBot()
        if (content.isNotBlank()) {
            player["task"] = content
        }
        bot.events.emit(StartBot)
    }
}

var counter = 0

on<Command>({ prefix == "bots" }) { _: Player ->
    val count = content.toIntOrNull() ?: 1
    val lumbridge = Rectangle(3221, 3217, 3222, 3220)
    val tile = lumbridge.random()
    GlobalScope.launch {
        repeat(count) {
            if (it % 25 == 0) {
                suspendCancellableCoroutine<Unit> { cont ->
                    World.run("bot_${counter}", 0) {
                        cont.resume(Unit)
                    }
                }
            }
            GlobalScope.launch(Contexts.Game) {
                val name = "Bot ${++counter}"
                val index = gatekeeper.connect(name)!!
                val bot = accounts.getOrElse(name, index) { Player(index = index, tile = tile, accountName = name) }
                setAppearance(bot)
                queue.await()
                if (bot.inventory.isEmpty()) {
                    bot.inventory.add("coins", 10000)
                }
                val client = if (TaskManager.DEBUG) DummyClient() else null
                bot.initBot()
                bot.login(client, 0)
                bot.events.emit(StartBot)
                bot.viewport?.loaded = true
                pause(3)
                bots.add(bot)
                bot.running = true
            }
        }
    }
}

fun Player.initBot(): Bot {
    val bot = Bot(this)
    get<EventHandlerStore>().populate(Bot::class, bot.botEvents)
    this["bot"] = bot
    val e = ConcurrentLinkedQueue<Event>()
    this["events"] = e
    events.all = { event ->
        e.add(event)
        handleSuspensions(bot.player, event)
    }
    return bot
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

fun setAppearance(player: Player): Player {
    val male = random.nextBoolean()
    player.body.male = male
    val key = "look_hair_${if (male) "male" else "female"}"
    player.body.setLook(BodyPart.Hair, enums.getStruct(key, random.nextInt(0, enums.get(key).length), "body_look_id"))
    player.body.setLook(BodyPart.Beard, if (male) enums.get("look_beard_male").randomInt() else -1)
    val size = enums.get("character_styles").length
    val style = enums.getStruct("character_styles", (0 until size).random(), "character_creation_sub_style_${player.sex}_0", -1)
    val struct = structs.get(style)
    player.body.setLook(BodyPart.Chest, struct["character_style_top"])
    player.body.setLook(BodyPart.Arms, struct["character_style_arms"])
    player.body.setLook(BodyPart.Hands, struct["character_style_wrists"])
    player.body.setLook(BodyPart.Legs, struct["character_style_legs"])
    player.body.setLook(BodyPart.Feet, struct["character_style_shoes"])
    val offset = random.nextInt(0, 8)
    player.body.setColour(BodyColour.Hair, enums.get("colour_hair").randomInt())
    player.body.setColour(BodyColour.Top, struct["character_style_top_colour_$offset"])
    player.body.setColour(BodyColour.Legs, struct["character_style_legs_colour_$offset"])
    player.body.setColour(BodyColour.Feet, struct["character_style_shoes_colour_$offset"])
    player.body.setColour(BodyColour.Skin, enums.get("character_skin").randomInt())
    player.appearance.emote = 1426
    return player
}
package world.gregs.voidps.bot

import kotlinx.coroutines.*
import world.gregs.voidps.engine.Contexts
import world.gregs.voidps.engine.client.ConnectionGatekeeper
import world.gregs.voidps.engine.client.ConnectionQueue
import world.gregs.voidps.engine.client.ui.event.adminCommand
import world.gregs.voidps.engine.data.PlayerAccounts
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.StructDefinitions
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.appearance
import world.gregs.voidps.engine.entity.character.player.sex
import world.gregs.voidps.engine.entity.worldSpawn
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventStore
import world.gregs.voidps.engine.getIntProperty
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.engine.timer.worldTimerStart
import world.gregs.voidps.engine.timer.worldTimerTick
import world.gregs.voidps.network.client.DummyClient
import world.gregs.voidps.network.visual.update.player.BodyColour
import world.gregs.voidps.network.visual.update.player.BodyPart
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.reflect.KClass

val areas: AreaDefinitions by inject()
val lumbridge = areas["lumbridge_teleport"]
val botCount = getIntProperty("bots", 0)

val bots = mutableListOf<Player>()
val queue: ConnectionQueue by inject()
val gatekeeper: ConnectionGatekeeper by inject()
val accounts: PlayerAccounts by inject()
val enums: EnumDefinitions by inject()
val structs: StructDefinitions by inject()

var counter = 0

worldSpawn {
    if (botCount > 0) {
        World.timers.start("bot_spawn")
    }
    EventStore.events.all = { player, event ->
        handleSuspensions(player, event)
    }
}

worldTimerStart("bot_spawn") {
    interval = TimeUnit.MINUTES.toTicks(1)
}

worldTimerTick("bot_spawn") {
    if (counter > botCount) {
        cancel()
        return@worldTimerTick
    }
    spawn()
}

adminCommand("bots") {
    val count = content.toIntOrNull() ?: 1
    GlobalScope.launch {
        repeat(count) {
            if (it % 25 == 0) {
                suspendCancellableCoroutine { cont ->
                    World.queue("bot_${counter}") {
                        cont.resume(Unit)
                    }
                }
                spawn()
            }
        }
    }
}

adminCommand("bot") {
    if (player.isBot) {
        player.clear("bot")
    } else {
        val bot = player.initBot()
        if (content.isNotBlank()) {
            player["task"] = content
        }
        bot.emit(StartBot)
    }
}

fun spawn() {
    GlobalScope.launch(Contexts.Game) {
        val name = "Bot ${++counter}"
        val index = gatekeeper.connect(name)!!
        val bot = accounts.getOrElse(name, index) { Player(index = index, tile = lumbridge.random(), accountName = name) }
        setAppearance(bot)
        queue.await()
        if (bot.inventory.isEmpty()) {
            bot.inventory.add("coins", 10000)
        }
        val client = if (TaskManager.DEBUG) DummyClient() else null
        bot.initBot()
        bot.login(client, 0)
        bot.emit(StartBot)
        bot.viewport?.loaded = true
        delay(3)
        bots.add(bot)
        bot.running = true
    }
}

fun Player.initBot(): Bot {
    val bot = Bot(this)
    this["bot"] = bot
    return bot
}

fun handleSuspensions(player: Player, event: Event) {
    val suspensions: MutableMap<KClass<*>, Pair<Event.(Player) -> Boolean, CancellableContinuation<Unit>>> = player["bot_suspensions"] ?: return
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
    player.body.setColour(BodyColour.Top, struct["character_style_colour_top_$offset"])
    player.body.setColour(BodyColour.Legs, struct["character_style_colour_legs_$offset"])
    player.body.setColour(BodyColour.Feet, struct["character_style_colour_shoes_$offset"])
    player.body.setColour(BodyColour.Skin, enums.get("character_skin").randomInt())
    player.appearance.emote = 1426
    return player
}

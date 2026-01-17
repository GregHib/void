package content.bot

import kotlinx.coroutines.*
import world.gregs.voidps.engine.Contexts
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.PlayerAccountLoader
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.intArg
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.AccountManager
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.AreaTypes
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.StructDefinitions
import world.gregs.voidps.engine.entity.MAX_PLAYERS
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.appearance
import world.gregs.voidps.engine.entity.character.player.sex
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.timer.*
import world.gregs.voidps.network.client.DummyClient
import world.gregs.voidps.network.login.protocol.visual.update.player.BodyColour
import world.gregs.voidps.network.login.protocol.visual.update.player.BodyPart
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.text.toIntOrNull

class BotSpawns(
    val enums: EnumDefinitions,
    val structs: StructDefinitions,
    val tasks: TaskManager,
    val loader: PlayerAccountLoader,
) : Script {

    val bots = mutableListOf<Player>()

    var counter = 0

    init {
        timerStart("bot_spawn") { TimeUnit.SECONDS.toTicks(Settings["bots.spawnSeconds", 60]) }

        timerTick("bot_spawn") {
            if (counter > Settings["bots.count", 0]) {
                return@timerTick Timer.CANCEL
            }
            spawn()
            return@timerTick Timer.CONTINUE
        }

        worldSpawn {
            if (Settings["bots.count", 0] > 0) {
                World.timers.start("bot_spawn")
            }
        }

        settingsReload {
            if (Settings["bots.count", 0] > bots.size) {
                World.timers.start("bot_spawn")
            }
        }

        adminCommand("bots", intArg("count", optional = true), desc = "Spawn (count) number of bots", handler = ::spawn)
        adminCommand("clear_bots", intArg("count", optional = true), desc = "Clear all or some amount of bots", handler = ::clear)
        adminCommand("bot", stringArg("task", optional = true, autofill = tasks.names), desc = "Toggle yourself on/off as a bot player", handler = ::toggle)
    }

    fun spawn(player: Player, args: List<String>) {
        val count = args[0].toIntOrNull() ?: 1
        GlobalScope.launch {
            repeat(count) {
                if (it % Settings["network.maxLoginsPerTick", 25] == 0) {
                    suspendCancellableCoroutine { cont ->
                        World.queue("bot_$counter") {
                            cont.resume(Unit)
                        }
                    }
                }
                spawn()
            }
        }
    }

    fun clear(player: Player, args: List<String>) {
        val count = args[0].toIntOrNull() ?: MAX_PLAYERS
        World.queue("bot_$counter") {
            val manager = get<AccountManager>()
            runBlocking {
                for (bot in bots.take(count)) {
                    manager.logout(bot, false)
                }
            }
        }
    }

    fun toggle(player: Player, args: List<String>) {
        if (player.isBot) {
            player.clear("bot")
            player.message("Bot disabled.")
        } else {
            player.initBot()
            if (args[0].isNotBlank()) {
                player["task_bot"] = args[0]
            }
            Bots.start(player)
            player.message("Bot enabled.")
        }
    }

    fun spawn() {
        GlobalScope.launch(Contexts.Game) {
            val name = "Bot ${++counter}"
            val bot = Player(tile = AreaTypes["lumbridge_teleport"].random(), accountName = name)
            bot.initBot()
            loader.connect(bot, if (Settings["development.bots.live", false]) DummyClient() else null)
            setAppearance(bot)
            if (bot.inventory.isEmpty()) {
                bot.inventory.add("coins", 10000)
            }
            Bots.start(bot)
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
}

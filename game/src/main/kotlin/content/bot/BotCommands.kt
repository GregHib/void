@file:OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)

package content.bot

import content.bot.allocation.BotProvisioner
import content.bot.allocation.BotRole
import content.bot.allocation.BotSpawnStrategy
import content.bot.allocation.BotSpawns
import content.entity.combat.killer
import content.quest.questJournal
import kotlinx.coroutines.*
import world.gregs.voidps.engine.Contexts
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.PlayerAccountLoader
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.intArg
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.AccountManager
import world.gregs.voidps.engine.data.SaveQueue
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.StructDefinitions
import world.gregs.voidps.engine.entity.MAX_PLAYERS
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.appearance
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.sex
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.timer.*
import world.gregs.voidps.network.client.DummyClient
import world.gregs.voidps.network.login.protocol.visual.update.player.BodyColour
import world.gregs.voidps.network.login.protocol.visual.update.player.BodyPart
import world.gregs.voidps.type.random
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.text.toIntOrNull
import kotlin.time.Duration.Companion.milliseconds

class BotCommands(
    val loader: PlayerAccountLoader,
    val manager: BotManager,
    val accounts: AccountManager,
    private val saveQueue: SaveQueue,
    accountDefinitions: AccountDefinitions,
) : Script {

    val bots = mutableListOf<Player>()
    val names = mutableListOf<String>()
    private val allNames = mutableSetOf<String>()
    private val loading = mutableSetOf<String>()

    var counter = 0
    private val provisioner = BotProvisioner(manager::activity)

    /**
     * Number of bots spawned per allocation, for allocations which don't refill
     */
    private val spawned = mutableMapOf<String, Int>()

    /**
     * Game tick each allocation can next spawn a bot
     */
    private val nextSpawn = mutableMapOf<String, Int>()

    /**
     * Bots which have been allocated but are still logging in
     */
    private val pending = mutableListOf<Bot>()

    init {
        worldTimerStart("bot_allocations") { TimeUnit.SECONDS.toTicks(1) }

        worldTimerTick("bot_allocations") {
            if (spawnAllocations()) Timer.CONTINUE else Timer.CANCEL
        }

        playerDespawn {
            if (isBot) {
                manager.remove(bot)
            }
        }

        playerDeath {
            if (!isBot) return@playerDeath
            val bot = bot
            val allocation = BotSpawns.getOrNull(bot.allocation ?: return@playerDeath) ?: return@playerDeath
            val role = allocation.role(bot.role ?: return@playerDeath) ?: return@playerDeath
            val account = accountName
            World.queue("respawn_role_$account", initialDelay = 10) {
                val target = Players.find(account) ?: return@queue
                if (!target.isBot) return@queue
                refresh(target.bot, allocation, role)
            }
        }

        entered("*") { area ->
            if (!isBot || dead) return@entered
            val bot = bot
            val allocation = BotSpawns.getOrNull(bot.allocation ?: return@entered) ?: return@entered
            if (area.name !in allocation.config.restockAreas) return@entered
            val role = allocation.role(bot.role ?: return@entered) ?: return@entered
            refresh(bot, allocation, role)
        }

        playerDeath {
            if (isBot) return@playerDeath
            val slayer = killer as? Player ?: return@playerDeath
            if (!slayer.isBot) return@playerDeath
            slayer.start("loot_pending", LOOT_PENDING_TICKS)
            slayer["loot_drop_tile"] = tile.id
        }

        worldSpawn {
            loadSettings()
        }

        settingsReload {
            loadSettings()
        }

        adminCommand("bots", intArg("count", optional = true), stringArg("allocation", optional = true, autofill = { BotSpawns.ids }), desc = "Spawn (count) number of bots into an allocation (default world)", handler = ::spawn)
        adminCommand("clear_bots", intArg("count", optional = true), desc = "Clear all or some amount of bots", handler = ::clear)
        adminCommand("bot", stringArg("task", optional = true, autofill = manager.activityNames), desc = "Toggle yourself on/off as a bot player", handler = ::toggle)
        adminCommand("bot_info", stringArg("name", optional = true, desc = "Filter by bot name", autofill = accountDefinitions.displayNames.keys), desc = "Print bot info", handler = ::info)
        adminCommand("bot_stress", intArg("ticks", optional = true), intArg("warmup", optional = true), desc = "Measure BotManager perf for N ticks (default 500); optional warmup ticks delay measurement.", handler = ::botStress)
    }

    @Suppress("UNUSED_PARAMETER")
    fun botStress(player: Player, args: List<String>) {
        val ticks = args.getOrNull(0)?.toIntOrNull() ?: 500
        val warmup = args.getOrNull(1)?.toIntOrNull() ?: 0
        if (ticks <= 0) {
            player.message("bot_stress: ticks must be > 0.", ChatType.Console)
            return
        }
        if (BotMetrics.measuring) {
            player.message("bot_stress: a measurement is already running.", ChatType.Console)
            return
        }
        val invokerName = player.accountName
        val onComplete: (List<String>) -> Unit = { lines ->
            val target = Players.find(invokerName)
            if (target != null) {
                for (line in lines) {
                    target.message(line, ChatType.Console)
                }
            }
        }
        if (warmup > 0) {
            player.message("bot_stress: warmup=$warmup ticks, then measure ticks=$ticks.", ChatType.Console)
            World.queue("bot_stress_warmup", initialDelay = warmup) {
                BotMetrics.start(ticks, label = "bots=${manager.bots.size}", onComplete = onComplete)
            }
        } else {
            player.message("bot_stress: measuring ticks=$ticks (bots=${manager.bots.size}).", ChatType.Console)
            BotMetrics.start(ticks, label = "bots=${manager.bots.size}", onComplete = onComplete)
        }
    }

    private fun loadSettings() {
        BotSpawns.load { manager.activity(it) != null }
        var start = false
        for (allocation in BotSpawns.all) {
            if (allocation.count <= 0) {
                continue
            }
            // First spawn after one interval, as with a regular timer
            nextSpawn.putIfAbsent(allocation.id, GameLoop.tick + TimeUnit.SECONDS.toTicks(allocation.spawnSeconds))
            start = true
        }
        if (start) {
            World.timers.start("bot_allocations")
        }
        if (!Settings["bots.numberedNames", false]) {
            names.clear()
            names.addAll(File(Settings["bots.names"]).readLines())
            allNames.clear()
            allNames.addAll(names.map { it.lowercase() })
        }
    }

    fun spawn(player: Player, args: List<String>) {
        val count = args.getOrNull(0)?.toIntOrNull() ?: 1
        val id = args.getOrNull(1)?.takeIf { it.isNotBlank() } ?: "world"
        val allocation = BotSpawns.getOrNull(id)
        if (allocation == null) {
            player.message("Unknown bot allocation '$id'. Options: ${BotSpawns.ids.joinToString()}.", ChatType.Console)
            return
        }
        GlobalScope.launch {
            repeat(count) {
                if (it % Settings["network.maxLoginsPerTick", 25] == 0) {
                    suspendCancellableCoroutine { cont ->
                        World.queue("bot_$counter") {
                            cont.resume(Unit)
                        }
                    }
                }
                spawnBot(allocation)
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun clear(player: Player, args: List<String>) {
        val count = args.getOrNull(0)?.toIntOrNull() ?: MAX_PLAYERS
        World.queue("bot_clear") {
            runBlocking {
                var removed = 0
                for (bot in manager.bots.toList()) {
                    if (bot.player.client != null && bot.player.client !is DummyClient) {
                        continue
                    }
                    manager.remove(bot)
                    Script.launch {
                        accounts.logout(bot.player, true)
                    }
                    if (++removed >= count) {
                        break
                    }
                }
            }
        }
    }

    fun info(player: Player, args: List<String>) {
        if (player.isBot) {
            player.message("Available activities:", ChatType.Console)
            for (activity in player.bot.available) {
                player.message("  $activity", ChatType.Console)
            }
        }
        val filter = args.getOrNull(0)
        val info = mutableListOf<String>()
        for (bot in manager.bots) {
            if (filter != null && !filter.equals(bot.player.name, ignoreCase = true)) {
                continue
            }
            info.add(bot.player.name)
            for (frame in bot.frames) {
                info.add("<blue>${frame.behaviour.id} - ${frame.state}")
            }
        }
        player.questJournal("Bots List", info.take(300))
    }

    fun toggle(player: Player, args: List<String>) {
        if (player.isBot) {
            manager.remove(player.bot)
            player.clear("bot")
            player.message("Bot disabled.")
        } else {
            val bot = player.initBot()
            manager.add(bot)
            if (args.getOrNull(0)?.isNotBlank() == true) {
                bot.available.clear()
                val name = args[0]
                bot.blocked.remove(name)
                bot.available.add(name)
                manager.assign(bot, name)
            }
            player.message("Bot enabled.")
        }
    }

    /**
     * Spawn all [BotSpawns] which are due and below their target count.
     * @return whether any allocation still needs spawning
     */
    private fun spawnAllocations(): Boolean {
        var active = false
        for (allocation in BotSpawns.all) {
            val count = allocation.count
            if (count <= 0) {
                continue
            }
            if (allocation.config.refill) {
                active = true
                if (members(allocation.id).size >= count) {
                    continue
                }
            } else if ((spawned[allocation.id] ?: 0) >= count) {
                continue
            } else {
                active = true
            }
            if (GameLoop.tick < (nextSpawn[allocation.id] ?: 0)) {
                continue
            }
            nextSpawn[allocation.id] = GameLoop.tick + TimeUnit.SECONDS.toTicks(allocation.spawnSeconds)
            spawnBot(allocation)
        }
        return active
    }

    private fun members(allocation: String): List<Bot> = manager.bots.filter { it.allocation == allocation } + pending.filter { it.allocation == allocation }

    /**
     * Spawn a bot into [allocation], giving it a role and the levels and items it needs if the allocation assigns one.
     */
    fun spawnBot(allocation: BotSpawnStrategy) {
        spawned[allocation.id] = (spawned[allocation.id] ?: 0) + 1
        GlobalScope.launch(Contexts.Game) {
            counter++
            val name = (if (allocation.config.saved) pickSavedBot() else null) ?: pickBotName()
            val tile = Areas[allocation.config.spawnAreas.random(random)].random()
            val saved = saveQueue.loadBot(name)?.toPlayer()
            val player = saved ?: Player(tile = tile, accountName = name)
            val bot = player.initBot()
            bot.allocation = allocation.id
            // Allocate before suspending so bots spawned together are counted
            val role = allocation.allocate(members(allocation.id))
            bot.role = role?.id
            if (role != null) {
                player.tile = tile
            }
            pending.add(bot)
            try {
                loader.connect(player, DummyClient(), viewport = Settings["development.bots.live", false])
                if (saved == null) {
                    setAppearance(player)
                    if (role == null) {
                        for ((id, amount) in allocation.config.starterItems) {
                            player.inventory.add(id, amount)
                        }
                    }
                }
                player.viewport?.loaded = true
                delay(3.milliseconds)
                if (role != null) {
                    provisioner.provision(bot, role, allocation.config.starterItems)
                }
                manager.add(bot)
                if (role != null) {
                    bot.pinned = role.activity
                    bot.available.clear()
                    bot.available.add(role.activity)
                    bot.blocked.remove(role.activity)
                    manager.assign(bot, role.activity)
                }
                player.running = true
            } finally {
                pending.remove(bot)
                loading.remove(name)
            }
        }
    }

    /**
     * Restock a role [bot] after death or on entering one of its allocation's restock areas and restart its role.
     */
    private fun refresh(bot: Bot, allocation: BotSpawnStrategy, role: BotRole) {
        provisioner.restore(bot, role, allocation.config.starterItems)
        manager.stop(bot)
        bot.blocked.remove(role.activity)
        bot.evaluate.clear()
        bot.previous = null
    }

    /**
     * Has a `bots.spawnSavedChance` of returning username of a saved bots which isn't already online.
     */
    private fun pickSavedBot(): String? {
        val chance = Settings["bots.spawnSavedChance", 0]
        if (chance <= 0 || random.nextInt(100) >= chance) {
            return null
        }
        val name = saveQueue.savedBotNames()
            .filter { isBotName(it) && Players.findByAccount(it) == null && it !in loading }
            .randomOrNull(random) ?: return null
        loading.add(name)
        // Stop the same name being picked randomly
        val prefix = Settings["bots.namePrefix", ""].trim('"')
        names.removeIf { "$prefix$it".equals(name, ignoreCase = true) }
        return name
    }

    /**
     * Checks if a saved account is a bot account or not.
     */
    private fun isBotName(name: String): Boolean {
        if (Settings["storage.bots.save", false] && Settings["storage.bots.separate", false] && Settings["storage.type", "files"] == "files") {
            return true
        }
        if (Settings["bots.numberedNames", false]) {
            return name.matches(NUMBERED_NAME)
        }
        val prefix = Settings["bots.namePrefix", ""].trim('"')
        return name.startsWith(prefix, ignoreCase = true) && allNames.contains(name.substring(prefix.length).lowercase())
    }

    fun Player.initBot(): Bot {
        val bot = Bot(this)
        this["bot"] = bot
        return bot
    }

    private fun pickBotName(): String {
        if (Settings["bots.numberedNames", false]) return "Bot $counter"
        val prefix = Settings["bots.namePrefix", ""].trim('"')
        val length = 12 - prefix.length
        val short = names.filter { it.length < length }
        val selected = short.randomOrNull(random) ?: names.removeAt(random.nextInt(names.size))
        names.remove(selected)
        return "$prefix$selected"
    }

    fun setAppearance(player: Player): Player {
        val male = random.nextBoolean()
        player.body.male = male
        val key = "look_hair_${if (male) "male" else "female"}"
        player.body.setLook(BodyPart.Hair, EnumDefinitions.getStruct(key, random.nextInt(0, EnumDefinitions.get(key).length), "body_look_id"))
        player.body.setLook(BodyPart.Beard, if (male) EnumDefinitions.get("look_beard_male").randomInt() else -1)
        val size = EnumDefinitions.get("character_styles").length
        val style = EnumDefinitions.getStruct("character_styles", (0 until size).random(), "character_creation_sub_style_${player.sex}_0", -1)
        val struct = StructDefinitions.get(style)
        player.body.setLook(BodyPart.Chest, struct["character_style_top"])
        player.body.setLook(BodyPart.Arms, struct["character_style_arms"])
        player.body.setLook(BodyPart.Hands, struct["character_style_wrists"])
        player.body.setLook(BodyPart.Legs, struct["character_style_legs"])
        player.body.setLook(BodyPart.Feet, struct["character_style_shoes"])
        val offset = random.nextInt(0, 8)
        player.body.setColour(BodyColour.Hair, EnumDefinitions.get("colour_hair").randomInt())
        player.body.setColour(BodyColour.Top, struct["character_style_colour_top_$offset"])
        player.body.setColour(BodyColour.Legs, struct["character_style_colour_legs_$offset"])
        player.body.setColour(BodyColour.Feet, struct["character_style_colour_shoes_$offset"])
        player.body.setColour(BodyColour.Skin, EnumDefinitions.get("character_skin").randomInt())
        player.appearance.emote = 1426
        return player
    }

    companion object {
        private const val LOOT_PENDING_TICKS = 60

        private val NUMBERED_NAME = Regex("Bot \\d+")
    }
}

@file:OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)

package content.bot

import com.github.michaelbull.logging.InlineLogger
import content.bot.behaviour.condition.BotEquipmentSetup
import content.bot.behaviour.condition.BotInventorySetup
import content.bot.combat.ClanWarsBotContext
import content.bot.combat.CombatBotContext
import content.bot.combat.CombatBotContexts
import content.bot.combat.CombatTier
import content.entity.combat.dead
import content.entity.combat.killer
import content.entity.player.combat.special.MAX_SPECIAL_ATTACK
import content.entity.player.combat.special.specialAttack
import content.entity.player.combat.special.specialAttackEnergy
import content.quest.questJournal
import kotlinx.coroutines.*
import world.gregs.voidps.engine.Contexts
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.PlayerAccountLoader
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.intArg
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.data.AccountManager
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
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.ClearItem.clear
import world.gregs.voidps.engine.timer.*
import world.gregs.voidps.network.client.DummyClient
import world.gregs.voidps.network.login.protocol.visual.update.player.BodyColour
import world.gregs.voidps.network.login.protocol.visual.update.player.BodyPart
import world.gregs.voidps.type.random
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.text.toIntOrNull

class BotCommands(
    val loader: PlayerAccountLoader,
    val manager: BotManager,
    val accounts: AccountManager,
    accountDefinitions: AccountDefinitions,
) : Script {

    private val combatBotsLogger = InlineLogger("CombatBots")
    val bots = mutableListOf<Player>()
    val names = mutableListOf<String>()
    private val combatBotTiers = mutableMapOf<String, CombatTier>()

    var counter = 0

    init {
        // Combat-bot contexts. New minigames register a CombatBotContext here so the generic
        // dispatchers below (playerDeath, entered, maintain timer, ::combatbots) pick them up
        // without touching this file.
        CombatBotContexts.register(ClanWarsBotContext())

        worldTimerStart("bot_spawn") { TimeUnit.SECONDS.toTicks(Settings["bots.spawnSeconds", 60]) }

        worldTimerTick("bot_spawn") {
            if (counter > Settings["bots.count", 0]) {
                return@worldTimerTick Timer.CANCEL
            }
            spawn()
            return@worldTimerTick Timer.CONTINUE
        }

        // Per-context auto-spawn timers — every arena that any registered context lists in
        // autospawnArenaKeys() gets its own maintain timer. Wired once at script init; the
        // timer body re-resolves the context in case settings reload changes it.
        for (context in CombatBotContexts.all()) {
            for (arenaKey in context.autospawnArenaKeys()) {
                val timerName = "combat_spawn_$arenaKey"
                worldTimerStart(timerName) { context.autospawnIntervalTicks(arenaKey) }
                worldTimerTick(timerName) {
                    maintainCombatArena(context, arenaKey)
                    Timer.CONTINUE
                }
            }
        }

        playerDespawn {
            if (isBot) {
                manager.remove(bot)
                combatBotTiers.remove(accountName)
            }
        }

        playerDeath {
            val tier = combatBotTiers[accountName] ?: return@playerDeath
            val context = CombatBotContexts.find(tier) ?: return@playerDeath
            if (get("debug", false)) {
                combatBotsLogger.info { "playerDeath fired for '$accountName', tier=${tier.activityId}, context=${context.id}" }
            }
            // Drop policy is the context's call (dangerous-arena bots drop kit; safe-arena
            // bots keep theirs to avoid duplicating the loadout on the floor after applyTier).
            if (!context.shouldDropItems(this, tier)) {
                it.dropItems = false
            }
            // Override default home respawn — drop the bot back inside the arena's spawn area.
            val respawn = context.respawnTile(tier)
            if (respawn != null) {
                it.teleport = respawn
            }
            val capturedAccount = accountName
            World.queue("respawn_tier_$capturedAccount", initialDelay = 10) {
                val target = Players.find(capturedAccount) ?: return@queue
                if (!target.isBot) return@queue
                val freshBot = target.bot
                applyTier(freshBot, tier)
                manager.stop(freshBot)
                freshBot.blocked.remove(tier.activityId)
                freshBot.evaluate.clear()
                freshBot.previous = null
            }
        }

        playerDeath {
            // PvP bots have dropItems=false in the prior handler, so no loot to scan for.
            if (isBot) return@playerDeath
            val slayer = killer as? Player ?: return@playerDeath
            if (!slayer.isBot) return@playerDeath
            slayer.start("loot_pending", LOOT_PENDING_TICKS)
            slayer["loot_drop_tile"] = tile.id
        }

        // Wire one `entered` listener per area any context subscribes to. The handler
        // dispatches by tier → context. Adding a new subscribed area in a new context works
        // automatically as long as that context is registered before this init block runs.
        for (areaId in CombatBotContexts.subscribedAreas()) {
            entered(areaId) {
                val tier = combatBotTiers[accountName] ?: return@entered
                val context = CombatBotContexts.find(tier) ?: return@entered
                // Death sequence also fires entered(...) while dead is still true; skip those
                // — playerDeath already schedules a delayed applyTier and we don't want to
                // double up.
                if (dead) return@entered
                context.onAreaEntered(this, tier, areaId)
                if (!context.shouldRefreshOnAreaEntered(this, tier, areaId)) return@entered
                // Restock kit before any portal-entry resolver walks the bot back; without
                // this the bot re-enters the arena with empty inventory and gets stuck/killed.
                val freshBot = bot
                applyTier(freshBot, tier)
                manager.stop(freshBot)
                freshBot.blocked.remove(tier.activityId)
                freshBot.evaluate.clear()
                freshBot.previous = null
            }
        }

        worldSpawn {
            loadSettings()
        }

        settingsReload {
            loadSettings()
        }

        adminCommand("bots", intArg("count", optional = true), desc = "Spawn (count) number of bots", handler = ::spawn)
        adminCommand("clear_bots", intArg("count", optional = true), desc = "Clear all or some amount of bots", handler = ::clear)
        adminCommand("bot", stringArg("task", optional = true, autofill = manager.activityNames), desc = "Toggle yourself on/off as a bot player", handler = ::toggle)
        adminCommand("bot_info", stringArg("name", optional = true, desc = "Filter by bot name", autofill = accountDefinitions.displayNames.keys), desc = "Print bot info", handler = ::info)
        adminCommand("combatbots", stringArg("arena", autofill = { CombatBotContexts.all().flatMap { it.arenaKeys() }.toSet() }), intArg("count", optional = true), desc = "Spawn combat bots for a named arena", handler = ::combatBots)
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
        if (Settings["bots.count", 0] > 0) {
            World.timers.start("bot_spawn")
        }
        if (!Settings["bots.numberedNames", false]) {
            names.clear()
            names.addAll(File(Settings["bots.names"]).readLines())
        }
        CombatBotContexts.loadAll()
        for (context in CombatBotContexts.all()) {
            for (arenaKey in context.autospawnArenaKeys()) {
                if (context.autospawnTarget(arenaKey) > 0) {
                    World.timers.start("combat_spawn_$arenaKey")
                }
            }
        }
    }

    private fun maintainCombatArena(context: CombatBotContext, arenaKey: String) {
        val target = context.autospawnTarget(arenaKey)
        if (target <= 0) return
        val current = combatBotTiers.values.count { context.arenaContains(arenaKey, it) }
        if (current >= target) return
        spawnCombatBot(context, arenaKey)
    }

    @Suppress("UNUSED_PARAMETER")
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

    @Suppress("UNUSED_PARAMETER")
    fun clear(player: Player, args: List<String>) {
        val count = args.getOrNull(0)?.toIntOrNull() ?: MAX_PLAYERS
        World.queue("bot_clear") {
            runBlocking {
                var removed = 0
                for (bot in manager.bots) {
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

    fun spawn() {
        GlobalScope.launch(Contexts.Game) {
            counter++
            val name = if (Settings["bots.numberedNames", false]) {
                "Bot $counter"
            } else {
                val prefix = Settings["bots.namePrefix", ""].trim('"')
                val length = 12 - prefix.length
                val short = names.filter { it.length < length }
                var selected = short.randomOrNull(random)
                if (selected == null) {
                    selected = names.removeAt(random.nextInt(names.size))
                } else {
                    names.remove(selected)
                }
                "${prefix}$selected"
            }
            val areas = setOf("lumbridge_teleport", "varrock_teleport", "draynor_bank")
            val player = Player(tile = Areas[areas.random()].random(), accountName = name)
            val bot = player.initBot()
            loader.connect(player, DummyClient(), viewport = Settings["development.bots.live", false])
            setAppearance(player)
            player.inventory.add("coins", 10000)
            player.viewport?.loaded = true
            delay(3)
            manager.add(bot)
            player.running = true
        }
    }

    fun Player.initBot(): Bot {
        val bot = Bot(this)
        this["bot"] = bot
        return bot
    }

    fun combatBots(player: Player, args: List<String>) {
        val arenaKey = args[0]
        val context = CombatBotContexts.forArenaKey(arenaKey)
        if (context == null) {
            val keys = CombatBotContexts.all().flatMap { it.arenaKeys() }.joinToString()
            player.message("Unknown arena '$arenaKey'. Options: $keys.", ChatType.Console)
            return
        }
        val count = args.getOrNull(1)?.toIntOrNull() ?: 14
        GlobalScope.launch {
            repeat(count) { index ->
                if (index % Settings["network.maxLoginsPerTick", 25] == 0) {
                    suspendCancellableCoroutine { cont ->
                        World.queue("combatbot_$counter") { cont.resume(Unit) }
                    }
                }
                spawnCombatBot(context, arenaKey)
            }
        }
    }

    private fun spawnCombatBot(context: CombatBotContext, arenaKey: String) {
        GlobalScope.launch(Contexts.Game) {
            counter++
            val name = pickBotName()
            val spawn = context.arenaSpawn(arenaKey) ?: return@launch
            val bot = Player(tile = spawn, accountName = name).initBot()
            loader.connect(bot.player, DummyClient(), viewport = Settings["development.bots.live", false])
            setAppearance(bot.player)
            delay(3)
            val tiers = context.arenaTiers(arenaKey)
            if (tiers.isEmpty()) return@launch
            val tier = tiers.random(random)
            combatBotTiers[bot.player.accountName] = tier
            applyTier(bot, tier)
            manager.add(bot)
            bot.pinned = tier.activityId
            // Intentionally leave bot.refresh = null. BotManager.start's refresh path was
            // re-running applyTier every Pending tick whenever a setup item differed from
            // the template (e.g. dose-decremented potions, mid-fight spec weapon swaps),
            // which spun bots into a drink/eat/refresh loop. The surrounding `continue`
            // in start() still keeps pinned PvP bots out of bank-chest resolvers without
            // needing refresh; legitimate tier resets happen in the `playerDeath` handler.
            bot.available.clear()
            bot.available.add(tier.activityId)
            bot.blocked.remove(tier.activityId)
            manager.assign(bot, tier.activityId)
            bot.player.running = true
        }
    }

    private fun applyTier(bot: Bot, tier: CombatTier) {
        val target = bot.player
        for ((skill, level) in tier.levels) {
            val stored = if (skill == Skill.Constitution) level * 10 else level
            target.experience.set(skill, Level.experience(skill, stored))
            target.levels.set(skill, stored)
        }
        // Belt-and-braces full restore: HP + prayer to max (in case the tier omits one), special
        // attack energy to 100%, and clear any half-pressed spec toggle from the previous round.
        target.levels.clear(Skill.Constitution)
        target.levels.clear(Skill.Prayer)
        target.specialAttackEnergy = MAX_SPECIAL_ATTACK
        target.specialAttack = false
        target["combat_style"] = tier.style
        target["brew_doses_since_restore"] = 0
        target.stop("just_ate_food")
        val activity = manager.activity(tier.activityId) ?: return
        target.inventory.transaction { clear() }
        target.equipment.transaction { clear() }
        target.inventory.add("coins", 10000)
        for (condition in activity.setup) {
            when (condition) {
                is BotEquipmentSetup -> target.equipment.transaction {
                    for ((slot, item) in condition.items) {
                        val usable = item.ids.filter { it != "empty" && !it.endsWith("_noted") && !it.endsWith("_broken") }
                        val id = usable.randomOrNull() ?: item.ids.filter { it != "empty" }.randomOrNull() ?: continue
                        set(slot.index, Item(id, item.min ?: 1))
                    }
                }.also { ok -> if (!ok) combatBotsLogger.warn { "equipment transaction failed for ${target.accountName}: ${target.equipment.transaction.error}" } }
                is BotInventorySetup -> target.inventory.transaction {
                    for (item in condition.items) {
                        val id = item.ids.filter { it != "empty" }.randomOrNull() ?: continue
                        add(id, item.min ?: 1)
                    }
                }.also { ok -> if (!ok) combatBotsLogger.warn { "inventory transaction failed for ${target.accountName}: ${target.inventory.transaction.error}" } }
                else -> Unit
            }
        }
        val starting = activity.hybridStartingLoadout
        if (starting != null) {
            target["current_loadout"] = starting
            target["last_loadout_swap_tick"] = -10_000
        }
        for ((name, loadout) in activity.loadouts) {
            if (name == starting) continue
            target.inventory.transaction {
                for ((_, item) in loadout.equipment.items) {
                    val usable = item.ids.filter { it != "empty" && !it.endsWith("_noted") && !it.endsWith("_broken") }
                    val id = usable.randomOrNull() ?: item.ids.filter { it != "empty" }.randomOrNull() ?: continue
                    if (target.equipment.contains(id)) continue
                    if (target.inventory.contains(id)) continue
                    add(id, item.min ?: 1)
                }
                loadout.extraInventory?.items?.forEach { item ->
                    val id = item.ids.filter { it != "empty" }.randomOrNull() ?: return@forEach
                    add(id, item.min ?: 1)
                }
            }.also { ok -> if (!ok) combatBotsLogger.warn { "loadout '$name' didn't fit for ${target.accountName}: ${target.inventory.transaction.error}" } }
        }
        if (bot["debug", false]) {
            combatBotsLogger.info { "applyTier ${tier.activityId} for ${target.accountName}: levels=${tier.levels.map { "${it.key}=cur${target.levels.get(it.key)}/max${target.levels.getMax(it.key)}" }}" }
            combatBotsLogger.info { "  inventory=${(0 until target.inventory.size).mapNotNull { target.inventory.getOrNull(it) }.filter { it.id.isNotEmpty() }.map { "${it.id}x${it.amount}" }}" }
            combatBotsLogger.info { "  equipment=${(0 until target.equipment.size).mapNotNull { target.equipment.getOrNull(it) }.filter { it.id.isNotEmpty() }.map { "${it.id}x${it.amount}" }}" }
            for (condition in activity.setup) {
                combatBotsLogger.info { "  setup.check ${condition::class.simpleName} = ${condition.check(target)}" }
            }
        }
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
}

private const val LOOT_PENDING_TICKS = 60

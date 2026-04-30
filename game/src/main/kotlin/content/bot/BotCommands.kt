@file:OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)

package content.bot

import com.github.michaelbull.logging.InlineLogger
import content.bot.behaviour.condition.BotEquipmentSetup
import content.bot.behaviour.condition.BotInventorySetup
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
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.StructDefinitions
import world.gregs.voidps.engine.data.definition.Tables
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

    private val pvpLogger = InlineLogger("PvpBots")
    val bots = mutableListOf<Player>()
    val names = mutableListOf<String>()
    private val pvpBotTiers = mutableMapOf<String, PvpTier>()
    private val pvpArenas = mutableMapOf<String, PvpArena>()

    var counter = 0

    init {
        worldTimerStart("bot_spawn") { TimeUnit.SECONDS.toTicks(Settings["bots.spawnSeconds", 60]) }

        worldTimerTick("bot_spawn") {
            if (counter > Settings["bots.count", 0]) {
                return@worldTimerTick Timer.CANCEL
            }
            spawn()
            return@worldTimerTick Timer.CONTINUE
        }

        for (arenaKey in PVP_AUTOSPAWN_ARENAS) {
            val timerName = "pvp_spawn_$arenaKey"
            worldTimerStart(timerName) {
                TimeUnit.SECONDS.toTicks(Settings["bots.pvp.$arenaKey.spawnSeconds", 2])
            }
            worldTimerTick(timerName) {
                maintainPvpArena(arenaKey)
                Timer.CONTINUE
            }
        }

        playerDespawn {
            if (isBot) {
                manager.remove(bot)
                pvpBotTiers.remove(accountName)
            }
        }

        playerDeath {
            val tier = pvpBotTiers[accountName] ?: return@playerDeath
            if (get("debug", false)) {
                pvpLogger.info { "playerDeath fired for '$accountName', tier=${tier.activityId}, keys=${pvpBotTiers.keys}" }
            }
            // Bots in dangerous arenas drop their kit on death (loot piñata for the killer);
            // bots elsewhere keep their items so applyTier doesn't duplicate the kit on the floor.
            if (!isDangerousArenaDeath(this, tier)) {
                it.dropItems = false
            }
            // Override default home respawn — drop the bot back inside the arena's spawn area
            val arena = pvpArenas.values.firstOrNull { a -> a.tiers.any { t -> t.activityId == tier.activityId } }
            if (arena != null) {
                it.teleport = Areas[arena.spawnArea].random()
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
        }

        entered("clan_wars_teleport") {
            val tier = pvpBotTiers[accountName] ?: return@entered
            if (!tier.activityId.startsWith("clan_wars_ffa_dangerous_")) return@entered
            // Death sequence also teles to clan_wars_teleport while dead is still true; skip those —
            // playerDeath already schedules a delayed applyTier and we don't want to double up.
            if (dead) return@entered
            pvpLogger.info { "PvP bot retreat: '$accountName' tier=${tier.activityId} teleported to clan_wars_teleport" }
            // Restock kit before the portal-entry resolver walks the bot back into the arena;
            // without this the bot re-enters with empty inventory and gets stuck/killed.
            val freshBot = bot
            applyTier(freshBot, tier)
            manager.stop(freshBot)
            freshBot.blocked.remove(tier.activityId)
            freshBot.evaluate.clear()
            freshBot.previous = null
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
        adminCommand("pvpbots", stringArg("arena", autofill = { pvpArenas.keys }), intArg("count", optional = true), desc = "Spawn PvP bots for a named arena", handler = ::pvpBots)
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
        loadPvpArenas()
        for (arenaKey in PVP_AUTOSPAWN_ARENAS) {
            if (Settings["bots.pvp.$arenaKey.count", 0] > 0) {
                World.timers.start("pvp_spawn_$arenaKey")
            }
        }
    }

    private fun maintainPvpArena(arenaKey: String) {
        val target = Settings["bots.pvp.$arenaKey.count", 0]
        if (target <= 0) return
        val arena = pvpArenas[arenaKey] ?: return
        val tierPrefix = "${arenaKey}_"
        val current = pvpBotTiers.values.count { it.activityId.startsWith(tierPrefix) }
        if (current >= target) return
        spawnPvpBot(arena)
    }

    private fun isDangerousArenaDeath(deceased: Player, tier: PvpTier): Boolean {
        if (tier.activityId.startsWith("clan_wars_ffa_dangerous_")) return true
        return deceased.tile in Areas["clan_wars_ffa_dangerous_arena"]
    }

    private fun loadPvpArenas() {
        pvpArenas.clear()
        val arenaTable = Tables.getOrNull("clan_wars_arenas") ?: return
        val tierTable = Tables.getOrNull("clan_wars_tiers") ?: return
        val tiersById = tierTable.rows().associate { row -> row.rowId to row.toPvpTier() }
        for (row in arenaTable.rows()) {
            val spawnArea = row.string("spawn_area")
            val tiers = row.stringList("tiers").mapNotNull { tiersById[it] }
            if (tiers.isEmpty()) {
                pvpLogger.warn { "No tiers resolved for arena '${row.rowId}'." }
                continue
            }
            pvpArenas[row.rowId] = PvpArena(spawnArea, tiers)
        }
    }

    private fun RowDefinition.toPvpTier(): PvpTier {
        val skillNames = stringList("skills")
        val values = intList("levels")
        require(skillNames.size == values.size) { "clan_wars_tiers.$rowId: skills/levels size mismatch." }
        val levels = LinkedHashMap<Skill, Int>(skillNames.size)
        for ((index, name) in skillNames.withIndex()) {
            val skillId = Skill.map[name] ?: error("clan_wars_tiers.$rowId: unknown skill '$name'.")
            levels[Skill.all[skillId]] = values[index]
        }
        return PvpTier(activityId = rowId, levels = levels, style = string("combat_style"))
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

    fun pvpBots(player: Player, args: List<String>) {
        val arenaKey = args[0]
        val count = args.getOrNull(1)?.toIntOrNull() ?: 14
        val arena = pvpArenas[arenaKey]
        if (arena == null) {
            player.message("Unknown arena '$arenaKey'. Options: ${pvpArenas.keys.joinToString()}.", ChatType.Console)
            return
        }
        GlobalScope.launch {
            repeat(count) { index ->
                if (index % Settings["network.maxLoginsPerTick", 25] == 0) {
                    suspendCancellableCoroutine { cont ->
                        World.queue("pvpbot_$counter") { cont.resume(Unit) }
                    }
                }
                spawnPvpBot(arena)
            }
        }
    }

    private fun spawnPvpBot(arena: PvpArena) {
        GlobalScope.launch(Contexts.Game) {
            counter++
            val name = pickBotName()
            val spawn = Areas[arena.spawnArea].random()
            val bot = Player(tile = spawn, accountName = name).initBot()
            loader.connect(bot.player, DummyClient(), viewport = Settings["development.bots.live", false])
            setAppearance(bot.player)
            delay(3)
            val tier = arena.tiers.random(random)
            pvpBotTiers[bot.player.accountName] = tier
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

    private fun applyTier(bot: Bot, tier: PvpTier) {
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
                }.also { ok -> if (!ok) pvpLogger.warn { "equipment transaction failed for ${target.accountName}: ${target.equipment.transaction.error}" } }
                is BotInventorySetup -> target.inventory.transaction {
                    for (item in condition.items) {
                        val id = item.ids.filter { it != "empty" }.randomOrNull() ?: continue
                        add(id, item.min ?: 1)
                    }
                }.also { ok -> if (!ok) pvpLogger.warn { "inventory transaction failed for ${target.accountName}: ${target.inventory.transaction.error}" } }
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
            }.also { ok -> if (!ok) pvpLogger.warn { "loadout '$name' didn't fit for ${target.accountName}: ${target.inventory.transaction.error}" } }
        }
        if (bot["debug", false]) {
            pvpLogger.info { "applyTier ${tier.activityId} for ${target.accountName}: levels=${tier.levels.map { "${it.key}=cur${target.levels.get(it.key)}/max${target.levels.getMax(it.key)}" }}" }
            pvpLogger.info { "  inventory=${(0 until target.inventory.size).mapNotNull { target.inventory.getOrNull(it) }.filter { it.id.isNotEmpty() }.map { "${it.id}x${it.amount}" }}" }
            pvpLogger.info { "  equipment=${(0 until target.equipment.size).mapNotNull { target.equipment.getOrNull(it) }.filter { it.id.isNotEmpty() }.map { "${it.id}x${it.amount}" }}" }
            for (condition in activity.setup) {
                pvpLogger.info { "  setup.check ${condition::class.simpleName} = ${condition.check(target)}" }
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

private val PVP_AUTOSPAWN_ARENAS = listOf("clan_wars_ffa_safe", "clan_wars_ffa_dangerous")

private data class PvpArena(val spawnArea: String, val tiers: List<PvpTier>)

private data class PvpTier(
    val activityId: String,
    val levels: Map<Skill, Int>,
    val style: String,
)

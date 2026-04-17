@file:OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)

package content.bot

import com.github.michaelbull.logging.InlineLogger
import content.bot.behaviour.condition.BotEquipmentSetup
import content.bot.behaviour.condition.BotInventorySetup
import content.quest.questJournal
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
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.StructDefinitions
import world.gregs.voidps.engine.entity.MAX_PLAYERS
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.player.Player
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

        playerDespawn {
            if (isBot) {
                manager.remove(bot)
                pvpBotTiers.remove(accountName)
            }
        }

        playerDeath {
            pvpLogger.info { "playerDeath fired for '$accountName', tier=${pvpBotTiers[accountName]?.activityId}, keys=${pvpBotTiers.keys}" }
            val tier = pvpBotTiers[accountName] ?: return@playerDeath
            it.dropItems = false
            applyTier(bot, tier)
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
        adminCommand("pvpbots", stringArg("arena", autofill = PVP_ARENAS.keys), intArg("count", optional = true), desc = "Spawn PvP bots for a named arena", handler = ::pvpBots)
    }

    private fun loadSettings() {
        if (Settings["bots.count", 0] > 0) {
            World.timers.start("bot_spawn")
        }
        if (!Settings["bots.numberedNames", false]) {
            names.clear()
            names.addAll(File(Settings["bots.names"]).readLines())
        }
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
        val arena = PVP_ARENAS[arenaKey]
        if (arena == null) {
            player.message("Unknown arena '$arenaKey'. Options: ${PVP_ARENAS.keys.joinToString()}.", ChatType.Console)
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
            bot.player.viewport?.loaded = true
            bot.player["debug"] = true
            delay(3)
            val tier = arena.tiers.random(random)
            pvpBotTiers[bot.player.accountName] = tier
            applyTier(bot, tier)
            manager.add(bot)
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
            target.experience.set(skill, Level.experience(skill, level))
            target.levels.set(skill, if (skill == Skill.Constitution) level * 10 else level)
        }
        target["combat_style"] = tier.style
        val activity = manager.activity(tier.activityId) ?: return
        target.inventory.transaction { clear() }
        target.equipment.transaction { clear() }
        target.inventory.add("coins", 10000)
        for (condition in activity.setup) {
            when (condition) {
                is BotEquipmentSetup -> target.equipment.transaction {
                    for ((slot, item) in condition.items) {
                        val id = item.ids.filter { it != "empty" }.randomOrNull() ?: continue
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
        pvpLogger.info { "applyTier ${tier.activityId} for ${target.accountName}: levels=${tier.levels.map { "${it.key}=cur${target.levels.get(it.key)}/max${target.levels.getMax(it.key)}" }}" }
        pvpLogger.info { "  inventory=${(0 until target.inventory.size).mapNotNull { target.inventory.getOrNull(it) }.filter { it.id.isNotEmpty() }.map { "${it.id}x${it.amount}" }}" }
        pvpLogger.info { "  equipment=${(0 until target.equipment.size).mapNotNull { target.equipment.getOrNull(it) }.filter { it.id.isNotEmpty() }.map { "${it.id}x${it.amount}" }}" }
        for (condition in activity.setup) {
            pvpLogger.info { "  setup.check ${condition::class.simpleName} = ${condition.check(target)}" }
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

    companion object {
        private val MELEE_BASIC = mapOf(
            Skill.Attack to 70,
            Skill.Strength to 70,
            Skill.Defence to 60,
            Skill.Constitution to 70,
            Skill.Prayer to 43,
        )
        private val MELEE_INTERMEDIATE = mapOf(
            Skill.Attack to 75,
            Skill.Strength to 75,
            Skill.Defence to 65,
            Skill.Constitution to 75,
            Skill.Prayer to 55,
            Skill.Ranged to 65,
        )
        private val TANK = mapOf(
            Skill.Attack to 70,
            Skill.Strength to 70,
            Skill.Defence to 80,
            Skill.Constitution to 85,
            Skill.Prayer to 55,
        )
        private val PURE = mapOf(
            Skill.Attack to 75,
            Skill.Strength to 95,
            Skill.Defence to 1,
            Skill.Constitution to 85,
            Skill.Prayer to 70,
        )

        private val SAFE_TIERS = listOf(
            PvpTier("clan_wars_ffa_safe_basic_melee", MELEE_BASIC, "slash"),
            PvpTier("clan_wars_ffa_safe_basic_melee", MELEE_BASIC, "slash"),
            PvpTier("clan_wars_ffa_safe_basic_melee", MELEE_BASIC, "slash"),
            PvpTier("clan_wars_ffa_safe_intermediate_melee", MELEE_INTERMEDIATE, "slash"),
            PvpTier("clan_wars_ffa_safe_intermediate_melee", MELEE_INTERMEDIATE, "slash"),
            PvpTier("clan_wars_ffa_safe_advanced_tank", TANK, "slash"),
            PvpTier("clan_wars_ffa_safe_advanced_pure", PURE, "slash"),
        )

        private val DANGEROUS_TIERS = listOf(
            PvpTier("clan_wars_ffa_dangerous_basic_melee", MELEE_BASIC, "slash"),
            PvpTier("clan_wars_ffa_dangerous_basic_melee", MELEE_BASIC, "slash"),
            PvpTier("clan_wars_ffa_dangerous_basic_melee", MELEE_BASIC, "slash"),
            PvpTier("clan_wars_ffa_dangerous_intermediate_melee", MELEE_INTERMEDIATE, "slash"),
            PvpTier("clan_wars_ffa_dangerous_intermediate_melee", MELEE_INTERMEDIATE, "slash"),
            PvpTier("clan_wars_ffa_dangerous_advanced_tank", TANK, "slash"),
            PvpTier("clan_wars_ffa_dangerous_advanced_pure", PURE, "slash"),
        )

        private val PVP_ARENAS = mapOf(
            "clan_wars_ffa_safe" to PvpArena("clan_wars_teleport", SAFE_TIERS),
            "clan_wars_ffa_dangerous" to PvpArena("clan_wars_teleport", DANGEROUS_TIERS),
        )
    }
}

private data class PvpArena(val spawnArea: String, val tiers: List<PvpTier>)

private data class PvpTier(
    val activityId: String,
    val levels: Map<Skill, Int>,
    val style: String,
)

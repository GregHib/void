@file:OptIn(DelicateCoroutinesApi::class)

package content.bot.owned

import com.github.michaelbull.logging.InlineLogger
import content.area.wilderness.inMultiCombat
import content.area.wilderness.inPvp
import content.area.wilderness.inWilderness
import content.area.wilderness.wildernessLevel
import content.bot.Bot
import content.bot.BotManager
import content.bot.behaviour.Behaviour
import content.bot.behaviour.BotGameWorld
import content.bot.behaviour.HardReason
import content.bot.behaviour.Reason
import content.bot.behaviour.action.BotEscape
import content.bot.behaviour.action.BotFollowOwner
import content.bot.behaviour.action.BotGoToNearest
import content.bot.behaviour.action.BotInteractFloorItem
import content.bot.behaviour.action.BotOpenBank
import content.bot.behaviour.action.BotOwnedAttack
import content.bot.behaviour.action.BotOwnedBankTrip
import content.bot.behaviour.action.BotOwnedErrand
import content.bot.behaviour.action.BotOwnedGoTo
import content.bot.behaviour.action.BotOwnedResume
import content.bot.behaviour.action.BotWait
import content.bot.behaviour.activity.BotActivity
import content.bot.behaviour.condition.BotCombatLevel
import content.bot.behaviour.condition.BotMembers
import content.bot.behaviour.condition.BotOwnedCarrying
import content.bot.behaviour.condition.BotSkillLevel
import content.bot.behaviour.condition.Condition
import content.bot.bot
import content.bot.isBot
import content.entity.combat.attacker
import content.entity.combat.attacking
import content.entity.combat.target
import content.entity.combat.underAttack
import content.entity.player.bank.BankDeposit
import content.entity.player.bank.bank
import content.entity.player.bank.isNote
import content.entity.player.bank.noted
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.intEntry
import content.entity.player.dialogue.type.nameEntry
import content.entity.player.dialogue.type.npc
import content.entity.player.starterItems
import content.entity.player.dialogue.type.player
import content.entity.player.equip.Equipping
import content.entity.player.inv.item.addOrDrop
import content.skill.melee.weapon.fightStyle
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import world.gregs.voidps.engine.Contexts
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.PlayerAccountLoader
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.intArg
import world.gregs.voidps.engine.client.command.playerCommand
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.client.command.varArgs
import world.gregs.voidps.engine.client.instruction.InstructionHandlers
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.toDigitGroupString
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.Despawn
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnPlayerInteract
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.cantReach
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasRequirements
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.move
import world.gregs.voidps.engine.inv.moveToLimit
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.transact.operation.AddItemLimit.addToLimit
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.network.client.DummyClient
import world.gregs.voidps.network.client.instruction.InteractInterface
import world.gregs.voidps.network.client.instruction.InteractInterfaceItem
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile
import kotlin.coroutines.resume

/**
 * Player-owned bots: up to four bots a player hires from the Adventurers in the Servants' Guild. Each is a world bot ([Bot] on a
 * [DummyClient]) that mirrors its owner's stats and variables, shares the owner's bank, and only ever does
 * what the owner orders through `::bot` commands: a task from `owned.tasks.toml`, following (and, by
 * aggression, fighting beside) the owner, or standing by. The 634 client's plugin drives the same commands
 * and reads the bot state the server shows it through client variables ([OwnedBotVarcs]).
 *
 * Bots are never saved: the owner's save holds the roster ([OwnedRoster]) and, between snapshots, what each
 * bot carries. Items go back to the owner's bank whenever a bot leaves the world; a bot that dies is gone and
 * its slot must be bought again.
 */
class OwnedBots(
    private val loader: PlayerAccountLoader,
    private val manager: BotManager,
    private val accountDefinitions: AccountDefinitions,
    private val tasks: OwnedTasks,
    private val instructions: InstructionHandlers,
) : Script {

    private val logger = InlineLogger("OwnedBots")

    init {
        manager.failListener = ::failed

        playerCommand("bot", stringArg("action", autofill = ACTIONS), *varArgs("args"), desc = "Manage your owned bots; ::bot help lists the actions") { args ->
            command(this, args)
        }

        adminCommand("owned_bots", stringArg("action", optional = true, autofill = setOf("list", "inspect", "dismiss", "refund", "grant")), stringArg("player", optional = true), intArg("slot", optional = true), stringArg("name", optional = true), desc = "List, inspect, dismiss, refund or grant player-owned bots") { args ->
            admin(this, args)
        }

        worldSpawn {
            World.timers.start("owned_bot_sync")
            World.timers.start("owned_bot_snapshot")
        }

        worldTimerStart("owned_bot_sync") { 1 }

        worldTimerTick("owned_bot_sync") {
            sync()
            Timer.CONTINUE
        }

        worldTimerStart("owned_bot_snapshot") { Settings["bots.owned.snapshotTicks", 50].coerceAtLeast(1) }

        worldTimerTick("owned_bot_snapshot") {
            snapshotAll()
            Timer.CONTINUE
        }

        playerSpawn {
            if (!isBot && enabled()) {
                restore(this)
            }
        }

        playerDespawn {
            if (isBot) {
                forget(this)
            } else {
                dismissAll(this)
                clear("bot_ui")
            }
        }

        playerDeath {
            if (contains("owner")) {
                it.dropItems = false
                died(this)
            }
        }

        maxLevelChanged { _, _, _ ->
            if (!isBot) {
                for (bot in bots(this)) {
                    OwnedBotSetup.syncStats(this, bot.player)
                }
            }
        }

        for (key in MIRRORED) {
            variableSet(key) { _, _, to ->
                if (isBot) {
                    return@variableSet
                }
                for (bot in bots(this)) {
                    if (to == null) {
                        bot.player.clear(key)
                    } else {
                        bot.player[key] = to
                    }
                }
            }
        }

        inventoryUpdated("inventory") { _, _ ->
            itemsChanged(this)
        }

        inventoryUpdated("worn_equipment") { _, _ ->
            itemsChanged(this)
        }

        // Give/take/equip: the owner walks up to the bot (the engine's interaction, so an unreachable bot gives
        // "I can't reach that.") and goes through its pockets.
        playerOperate(TRANSFER) { (target) ->
            val transfer = get<Transfer>("owned_transfer") ?: return@playerOperate
            clear("owned_transfer")
            if (target.get<String>("owner") != accountName || target["owned_slot", -1] != transfer.slot) {
                return@playerOperate
            }
            // Beside it on the same floor; the movement only checks the ground plan
            val distance = tile.distanceTo(target.tile)
            if (distance < 0 || distance > 1) {
                cantReach()
                return@playerOperate
            }
            anim("pick_pocket")
            transfer.block(target)
        }

        npcOperate("Talk-to", "adventurer") { (target) ->
            hire(this, target)
        }

        // Each stands for one slot, the slots of a hall's four taken in spawn order: seen only by players with that
        // slot free, and by no one when bots are off here
        npcSpawn("adventurer") {
            val slot = NPCs.count { it !== this && it.id == id && it.tile.within(tile, HALL_RADIUS) }
            this["slot"] = slot
            visibleTo = { player -> enabled() && !player.isBot && slot < OwnedRoster.slots() && !OwnedRoster.alive(player, slot) }
        }
    }

    // ---- hiring ------------------------------------------------------------------------------------------

    /** An Adventurer in a general store, one for each slot: hired for that slot's price, they go off with the player. */
    private suspend fun hire(player: Player, adventurer: NPC) {
        if (!enabled() || player.isBot) {
            player.npc<Neutral>("Just passing through.")
            return
        }
        val slot = adventurer["slot", 0]
        if (slot >= OwnedRoster.slots() || OwnedRoster.alive(player, slot)) {
            player.npc<Neutral>("You look like you've all the help you can handle.")
            return
        }
        val price = OwnedRoster.price(slot)
        val again = OwnedRoster.name(player, slot) != null
        player.npc<Happy>(
            if (again) "Lost one of your crew? I'll take their place for ${price.toDigitGroupString()} coins." else "Looking for work, I am. ${price.toDigitGroupString()} coins and I'm yours: I'll fight at your side, gather whatever you point me at, and bank the lot.",
        )
        player.choice {
            option("Hire (${price.toDigitGroupString()} coins)") {
                purchase(player, slot, price)
            }
            option<Neutral>("What would you do for me?") {
                player.npc<Neutral>("Whatever you tell me: I fight and work as well as you do, keep what I gather in your bank, and answer to ::bot commands. Hire me once and I'm yours for life; if I die, that's that.")
                player.npc<Neutral>("${OwnedRoster.slots()} of us at most, ${OwnedRoster.price(0).toDigitGroupString()} coins each. Say ::bot help once you've hired one of us.")
            }
            option<Neutral>("Not today.")
        }
    }

    private suspend fun purchase(player: Player, slot: Int, price: Int) {
        if (!player.inventory.contains("coins", price)) {
            player.player<Sad>("I don't have ${price.toDigitGroupString()} coins on me.")
            player.npc<Neutral>("Come back when you do, then.")
            return
        }
        val name = player.nameEntry("What will you call them?").replace('_', ' ').trim()
        val error = OwnedRoster.validateName(name, accountDefinitions)
        if (error != null) {
            player.npc<Sad>(error)
            return
        }
        if (!player.inventory.remove("coins", price)) {
            player.npc<Neutral>("Come back when you have the coins.")
            return
        }
        OwnedRoster.purchase(player, slot, name)
        player.npc<Happy>("$name it is. Lead on! Say ::bot summon ${slot + 1} whenever you want me beside you.")
        // Off with the player: the slot is taken now, so they see this one no more (its visibility, set at spawn)
        summon(player, slot)
    }

    // ---- commands ----------------------------------------------------------------------------------------

    private suspend fun command(owner: Player, args: List<String>) {
        if (!enabled()) {
            owner.message("Player-owned bots aren't available on this world.")
            return
        }
        if (owner.isBot) {
            return
        }
        val action = args[0].lowercase()
        val rest = args.drop(1).filter { it.isNotBlank() }
        when (action) {
            "ui" -> ui(owner, rest.getOrNull(0))
            "tasks" -> listTasks(owner)
            "status" -> status(owner)
            "summon" -> withSlot(owner, rest) { summon(owner, it) }
            "dismiss" -> forSlots(owner, rest) { dismiss(owner, it) }
            "call" -> forSlots(owner, rest) { call(owner, it) }
            "task" -> withSlot(owner, rest) { assignTask(owner, it, rest.getOrNull(1)) }
            "follow" -> forSlots(owner, rest) { follow(owner, it) }
            "stop" -> forSlots(owner, rest) { stop(owner, it) }
            "guard", "hold" -> forSlots(owner, rest) { hold(owner, it) }
            "goto" -> forSlots(owner, rest) { goTo(owner, it, rest.getOrNull(1), rest.getOrNull(2)) }
            "attack" -> forSlots(owner, rest) { attack(owner, it, rest.getOrNull(1)) }
            "pickup" -> withSlot(owner, rest) { pickUp(owner, it, rest.getOrNull(1), rest.getOrNull(2), rest.getOrNull(3)) }
            "combat" -> withSlot(owner, rest) { combat(owner, it, rest.drop(1)) }
            "spec" -> forSlots(owner, rest) { spec(owner, it, rest.getOrNull(1)) }
            "aggression" -> withSlot(owner, rest) { aggression(owner, it, rest.getOrNull(1)) }
            "equip" -> withSlot(owner, rest) { equip(owner, it, rest.getOrNull(1), rest.getOrNull(2)) }
            "unequip" -> withSlot(owner, rest) { unequip(owner, it, rest.getOrNull(1)) }
            "give" -> withSlot(owner, rest) { give(owner, it, rest.getOrNull(1), rest.getOrNull(2), rest.getOrNull(3)) }
            "take" -> withSlot(owner, rest) { take(owner, it, rest.getOrNull(1), rest.getOrNull(2), rest.getOrNull(3)) }
            "bank" -> withSlot(owner, rest) { bankAll(owner, it) }
            "item" -> withSlot(owner, rest) { itemOption(owner, it, rest.getOrNull(1), rest.drop(2).joinToString(" ")) }
            "use" -> withSlot(owner, rest) { useItem(owner, it, rest.getOrNull(1), rest.getOrNull(2)) }
            "loot" -> withSlot(owner, rest) { loot(owner, it, rest.getOrNull(1)) }
            "rename" -> withSlot(owner, rest) { rename(owner, it, rest.getOrNull(1)) }
            "loadout" -> loadout(owner, rest)
            "show" -> withSlot(owner, rest) { show(owner, it) }
            else -> help(owner)
        }
    }

    private fun help(owner: Player) {
        owner.message("::bot summon|dismiss|call|follow|stop|guard <slot|all>  ::bot task <slot> <task>  ::bot tasks")
        owner.message("::bot goto <slot|all> <x> <y>  ::bot attack <slot|all> <npc index>  ::bot pickup <slot> <x> <y> <item>")
        owner.message("::bot equip <slot> <item> [bank]  ::bot unequip <slot> <weapon|shield|...>  ::bot bank <slot>")
        owner.message("::bot give|take <slot> <item> [amount|all] [bank]  (no amount: you're asked; bank: the bot goes to a booth)")
        owner.message("::bot item <slot> <inventory slot> <option>  ::bot use <slot> <inventory slot> <inventory slot>  (slots from 0)")
        owner.message("::bot combat <slot> <profile> [spec=item eat=40 flee=25]  ::bot spec <slot|all> <item>|none")
        owner.message("::bot aggression <slot> passive|aggressive  ::bot loot <slot> on|off  ::bot rename <slot> <name>  ::bot status|show <slot>")
        owner.message("::bot loadout save [name] [slot]|load <slot> <name>|list|delete <name>  (presets of worn gear; no slot = your own)")
    }

    private inline fun withSlot(owner: Player, rest: List<String>, block: (Int) -> Unit) {
        val slot = OwnedRoster.parseSlot(rest.getOrNull(0))
        if (slot == null) {
            owner.message("Which bot? Use a slot number from 1 to ${OwnedRoster.slots()}.")
            return
        }
        block(slot)
    }

    private inline fun forSlots(owner: Player, rest: List<String>, block: (Int) -> Unit) {
        if (rest.getOrNull(0).equals("all", ignoreCase = true)) {
            for (slot in 0 until OwnedRoster.slots()) {
                if (bot(owner, slot) != null) {
                    block(slot)
                }
            }
            return
        }
        withSlot(owner, rest, block)
    }

    private fun ui(owner: Player, token: String?) {
        if (token.equals("off", ignoreCase = true)) {
            owner.clear("bot_ui")
            return
        }
        // The plugin sends a control character no chat box can type, so a vanilla client never opts itself
        // into client variables its tables don't have.
        if (token == null || token.firstOrNull() != HANDSHAKE) {
            owner.message("The bot panel needs the 634 client's Owned Bots plugin.")
            return
        }
        owner["bot_ui"] = true
        OwnedBotVarcs.showAll(owner, slots(owner), tasks)
    }

    private fun listTasks(owner: Player) {
        owner.message("[BotTasks] ${tasks.all.size} tasks: ::bot task <slot> <task>")
        for (task in tasks.all) {
            owner.message("[BotTasks] ${task.index}|${task.id}|${task.label}")
        }
    }

    private fun status(owner: Player) {
        for (slot in 0 until OwnedRoster.slots()) {
            val name = OwnedRoster.name(owner, slot)
            val line = when {
                name == null -> "empty (hire an Adventurer at the Servants' Guild for ${OwnedRoster.price(slot).toDigitGroupString()} coins)"
                !OwnedRoster.alive(owner, slot) -> "$name - dead (hire another for ${OwnedRoster.price(slot).toDigitGroupString()} coins)"
                else -> {
                    val bot = bot(owner, slot)
                    if (bot == null) {
                        "$name - away (::bot summon ${slot + 1})"
                    } else {
                        val status = OwnedBotStatus.entries[bot.player["owned_status", OwnedBotStatus.Idle.ordinal]]
                        val task = bot.player.get<String>("owned_task")?.let { tasks.get(it)?.label }
                        "$name - ${status.name.lowercase()}${if (task != null) ": $task" else ""}, ${bot.player.levels.get(Skill.Constitution)} life points"
                    }
                }
            }
            owner.message("[Bot ${slot + 1}] $line")
        }
    }

    private fun show(owner: Player, slot: Int) {
        val bot = requireBot(owner, slot) ?: return
        val player = bot.player
        val worn = EquipSlot.entries.filter { it.index >= 0 }.mapNotNull { equip ->
            val item = player.equipment.getOrNull(equip.index) ?: return@mapNotNull null
            if (item.isEmpty()) null else "${equip.name.lowercase()}: ${item.id.toItemName()}${if (item.amount > 1) " x${item.amount}" else ""}"
        }
        owner.message("[Bot ${slot + 1}] Wearing: ${worn.ifEmpty { listOf("nothing") }.joinToString(", ")}")
        val carried = player.inventory.items.filter { it.isNotEmpty() }
            .groupBy { it.id }
            .map { (id, items) -> "${id.toItemName()}${items.sumOf { it.amount }.let { if (it > 1) " x$it" else "" }}" }
        owner.message("[Bot ${slot + 1}] Carrying: ${carried.ifEmpty { listOf("nothing") }.joinToString(", ")}")
    }

    // ---- lifecycle ---------------------------------------------------------------------------------------

    private fun summon(owner: Player, slot: Int) {
        val name = OwnedRoster.name(owner, slot)
        if (name == null || !OwnedRoster.alive(owner, slot)) {
            owner.message("You don't have a bot in slot ${slot + 1}. Adventurers for hire wait at the Servants' Guild in East Ardougne.")
            return
        }
        if (bot(owner, slot) != null) {
            owner.message("$name is already with you.")
            return
        }
        val account = accountName(owner, slot)
        if (owner["owned_bot_${slot}_summoning", false]) {
            return
        }
        val total = Settings["bots.owned.total", 0]
        if (total > 0 && liveCount() >= total) {
            owner.message("Too many bots are out in the world right now. Try again later.")
            return
        }
        if (owner.inPvp || owner.inWilderness) {
            owner.message("You can't summon a bot here.")
            return
        }
        owner["owned_bot_${slot}_summoning"] = true
        val tile = BotFollowOwner.beside(owner.tile)
        GlobalScope.launch(Contexts.Game) {
            try {
                // "creation" marks an account that has had its introduction: no welcome, no starter kit
                val player = Player(tile = tile, accountName = account, variables = mutableMapOf("display_name" to name, "creation" to System.currentTimeMillis(), "had_starter" to true))
                val bot = Bot(player)
                player["bot"] = bot
                loader.connect(player, DummyClient(), viewport = false)
                // Let the login settle for a tick (deterministic in ticks, unlike a wall-clock delay)
                suspendCancellableCoroutine { continuation ->
                    World.queue("owned_bot_summon_$account") { continuation.resume(Unit) }
                }
                if (Players.findByAccount(account) !== player) {
                    logger.warn { "Owned bot $account did not enter the world (found ${Players.findByAccount(account)?.index})." }
                    owner.message("$name couldn't be summoned right now.")
                    return@launch
                }
                if (Players.indexed(owner.index) !== owner) {
                    // The owner left while the bot was logging in: nobody to serve, nothing to sweep yet
                    player["logged_out"] = true
                    Players.remove(player)
                    Despawn.player(player)
                    return@launch
                }
                OwnedBotSetup.apply(owner, bot, slot)
                manager.add(bot)
                bot.available.clear()
                bot.pinnedSetup = true
                bot.pin(IDLE)
                player.running = true
                owner["owned_bot_${slot}_index"] = player.index
                // World timers are cleared with the world (tests, resets); make sure the sync is running
                World.timers.startIfAbsent("owned_bot_sync")
                World.timers.startIfAbsent("owned_bot_snapshot")
                setStatus(bot, OwnedBotStatus.Idle)
                if (owner["owned_bot_${slot}_kit", false]) {
                    // Freshly hired: the kit a new player starts with, theirs to use or bank
                    owner.clear("owned_bot_${slot}_kit")
                    starterItems(player)
                }
                player.say("Reporting for duty, ${owner.name}.")
                notify(owner, slot, "$name is ready.")
                // Falls in behind the owner, and first puts back on whatever it wore last time
                follow(owner, slot)
                val worn = Loadout.decode(OwnedRoster.worn(owner, slot))
                owner.clear("owned_bot_${slot}_worn")
                if (worn.worn.isNotEmpty()) {
                    enqueue(owner, bot, slot, *dress(player, worn.worn).toTypedArray())
                }
                refresh(owner)
            } catch (e: Exception) {
                logger.error(e) { "Failed to summon owned bot $account." }
                owner.message("$name couldn't be summoned right now.")
            } finally {
                owner.clear("owned_bot_${slot}_summoning")
            }
        }
    }

    private fun dismiss(owner: Player, slot: Int) {
        val bot = bot(owner, slot) ?: return
        owner.clear("owned_bot_${slot}_index")
        manager.remove(bot)
        sweep(owner, bot)
        owner.clear("owned_bot_${slot}_items")
        val player = bot.player
        player.clear("owner")
        player["logged_out"] = true
        // Leave the world next tick, and only if this bot is still the player at its index: a stale removal would
        // take a newer player's index with it.
        World.queue("owned_bot_dismiss_${player.accountName}") {
            if (Players.indexed(player.index) === player) {
                Players.remove(player)
                Despawn.player(player)
            }
        }
        refresh(owner)
    }

    private fun dismissAll(owner: Player) {
        for (slot in 0 until OwnedRoster.MAX_SLOTS) {
            dismiss(owner, slot)
        }
    }

    /** The bot left the world some other way (e.g. ::clear_bots). */
    private fun forget(player: Player) {
        val owner = player.owner ?: return
        val slot = player["owned_slot", -1]
        if (owner["owned_bot_${slot}_index", -1] == player.index) {
            owner.clear("owned_bot_${slot}_index")
            refresh(owner)
        }
    }

    /**
     * Everything the bot wears and carries goes into the owner's bank; what won't fit goes to the owner's
     * inventory, then the floor at the owner's feet.
     */
    private fun sweep(owner: Player, bot: Bot) {
        val player = bot.player
        val slot = player["owned_slot", -1]
        val worn = Loadout.capture(player.equipment, null)
        if (slot >= 0 && OwnedRoster.alive(owner, slot)) {
            if (worn.isEmpty()) owner.clear("owned_bot_${slot}_worn") else owner["owned_bot_${slot}_worn"] = worn.encode()
        }
        for (inventory in listOf(player.equipment, player.inventory)) {
            for (index in inventory.indices) {
                val item = inventory[index]
                if (item.isEmpty()) {
                    continue
                }
                BankDeposit.deposit(owner, inventory, item, item.amount, index, check = false)
                val left = inventory[index]
                if (left.isEmpty()) {
                    continue
                }
                if (inventory.transaction { remove(index, left.id, left.amount) }) {
                    owner.addOrDrop(left.id, left.amount)
                }
            }
        }
    }

    private fun died(player: Player) {
        val owner = player.owner ?: return
        val slot = player["owned_slot", -1]
        val bot = bot(owner, slot) ?: return
        setStatus(bot, OwnedBotStatus.Dead)
        OwnedRoster.kill(owner, slot)
        notify(owner, slot, "${player.name} has died. Its items have been returned to your bank.")
        val account = owner.accountName
        World.queue("owned_bot_death_${player.accountName}", 8) {
            val current = Players.findByAccount(account) ?: return@queue
            dismiss(current, slot)
        }
    }

    // ---- orders ------------------------------------------------------------------------------------------

    private fun assignTask(owner: Player, slot: Int, id: String?) {
        val bot = requireBot(owner, slot) ?: return
        val task = id?.let { tasks.get(it.lowercase()) }
        if (task == null) {
            owner.message("Which task? ::bot tasks lists them.")
            return
        }
        val base = manager.activity(task.activity)
        if (base == null) {
            owner.message("${task.label} isn't available right now.")
            return
        }
        // World bots spread by level bands; an owned bot does whatever its owner's levels allow.
        val activity = base.copy(
            id = "owned:${task.id}",
            requires = base.requires.map { if (it is BotSkillLevel) it.copy(max = null) else it },
        )
        // The picker would silently idle a bot that doesn't qualify; say why instead
        val unmet = activity.requires.firstOrNull { !it.check(bot.player) }
        if (unmet != null) {
            owner.message("[Bot ${slot + 1}] ${bot.player.name} can't do that: ${requirementText(bot.player, unmet)}")
            return
        }
        order(owner, bot, activity, task.id, OwnedBotStatus.Working, task.label)
    }

    private fun follow(owner: Player, slot: Int) {
        val bot = requireBot(owner, slot) ?: return
        val activity = OwnedProfiles.build(owner, bot, slot)
        bot.player["owned_aggression"] = OwnedRoster.aggression(owner, slot)
        bot.player.clear(BotEscape.RUN)
        order(owner, bot, activity, null, OwnedBotStatus.Following, "Following ${owner.name}.")
    }

    private fun stop(owner: Player, slot: Int) {
        val bot = requireBot(owner, slot) ?: return
        order(owner, bot, IDLE, null, OwnedBotStatus.Idle, "Standing by.")
    }

    /** Guard (`hold` is the old name): the bot keeps its place but fights as a follower would: defends itself and, by its aggression, the owner. */
    private fun hold(owner: Player, slot: Int) {
        val bot = requireBot(owner, slot) ?: return
        bot.player["owned_aggression"] = OwnedRoster.aggression(owner, slot)
        order(owner, bot, holdActivity(owner, bot, slot), null, OwnedBotStatus.Guarding, "Guarding here.")
    }

    private fun holdActivity(owner: Player, bot: Bot, slot: Int): BotActivity = OwnedProfiles.build(owner, bot, slot, HOLD_PREFIX, listOf(BotWait(10)))

    /**
     * What a one-off order (go somewhere, attack, pick up) goes back to when done: the follow or hold the bot had,
     * else standing by.
     */
    private fun resumeAfter(bot: Bot): BotActivity {
        val current = bot.pinnedActivity
        return if (current != null && (current.id.startsWith(FOLLOW_PREFIX) || current.id.startsWith(HOLD_PREFIX))) current else IDLE
    }

    /** The bot walks to the tile the owner pointed at and holds there. */
    private fun goTo(owner: Player, slot: Int, x: String?, y: String?) {
        val bot = requireBot(owner, slot) ?: return
        val tileX = x?.toIntOrNull()
        val tileY = y?.toIntOrNull()
        if (tileX == null || tileY == null) {
            owner.message("::bot goto <slot|all> <x> <y>")
            return
        }
        val tile = Tile(tileX, tileY, owner.tile.level)
        if (tile.distanceTo(owner.tile) > GOTO_RANGE) {
            owner.message("${bot.player.name} can only be sent somewhere near you.")
            return
        }
        if (owner.inPvp || owner.inWilderness || wildernessLevel(tile) > 0) {
            owner.message("${bot.player.name} won't go into a player-versus-player area.")
            return
        }
        bot.player["owned_aggression"] = OwnedRoster.aggression(owner, slot)
        val hold = holdActivity(owner, bot, slot)
        val activity = OwnedProfiles.build(owner, bot, slot, GOTO_PREFIX, listOf(BotOwnedGoTo(tile.x, tile.y), BotOwnedResume()), timeout = GOTO_TIMEOUT)
        order(owner, bot, activity, null, OwnedBotStatus.Working, "On my way.")
        bot.player["owned_resume"] = hold
    }

    /** The bot attacks the npc the owner pointed at (by index), then goes back to what it was doing. */
    private fun attack(owner: Player, slot: Int, index: String?) {
        val bot = requireBot(owner, slot) ?: return
        val npc = index?.toIntOrNull()?.let { NPCs.indexed(it) }
        if (npc == null) {
            owner.message("::bot attack <slot|all> <npc index>  (the client plugin's Attack with entry)")
            return
        }
        if (npc.dead) {
            owner.message("${npc.def(bot.player).name} is already dead.")
            return
        }
        if (npc.def(bot.player).options.indexOf("Attack") == -1) {
            owner.message("${npc.def(bot.player).name} can't be attacked.")
            return
        }
        if (npc.inPvp || npc.inWilderness || owner.inPvp || owner.inWilderness) {
            owner.message("${bot.player.name} won't fight in a player-versus-player area.")
            return
        }
        val player = bot.player
        if (npc.tile.distanceTo(player.tile) > ATTACK_RANGE) {
            owner.message("${player.name} is too far from that target.")
            return
        }
        // the same checks BotFightNpc makes before picking a target, so an order that can't be carried out is refused now
        val attacker = npc.attacker
        if (npc.underAttack && attacker != null && attacker !== player && !(player.inMultiCombat && npc.inMultiCombat)) {
            owner.message("${npc.def(player).name} is already fighting someone else.")
            return
        }
        if (!world.canReach(player, npc) && (player.fightStyle == "melee" || !world.canSee(player, npc))) {
            owner.message("${player.name} can't get at that target from here.")
            return
        }
        player["owned_aggression"] = OwnedRoster.aggression(owner, slot)
        val resume = resumeAfter(bot)
        val activity = OwnedProfiles.build(owner, bot, slot, ATTACK_PREFIX, listOf(BotOwnedAttack(npc.index), BotOwnedResume()), assist = false, timeout = ATTACK_TIMEOUT)
        order(owner, bot, activity, null, OwnedBotStatus.Fighting, "Attacking ${npc.def(bot.player).name}!")
        bot.player["owned_resume"] = resume
    }

    /** The bot walks to a floor item the owner pointed at and takes it, then goes back to what it was doing. */
    private fun pickUp(owner: Player, slot: Int, x: String?, y: String?, item: String?) {
        val bot = requireBot(owner, slot) ?: return
        val tileX = x?.toIntOrNull()
        val tileY = y?.toIntOrNull()
        val id = item?.let { itemId(it) }
        if (tileX == null || tileY == null || id == null) {
            owner.message("::bot pickup <slot> <x> <y> <item>")
            return
        }
        val tile = Tile(tileX, tileY, owner.tile.level)
        val floorItem = FloorItems.at(tile).firstOrNull { it.id == id && (it.owner == null || it.owner == bot.player.name) }
        if (floorItem == null) {
            owner.message("${bot.player.name} can't see ${id.toItemName()} there (an item you dropped stays yours alone for a minute).")
            return
        }
        if (tile.distanceTo(bot.player.tile) > ATTACK_RANGE) {
            owner.message("${bot.player.name} is too far from that item.")
            return
        }
        if (bot.player.inventory.isFull() && !(floorItem.def.stackable == 1 && bot.player.inventory.contains(id))) {
            owner.message("${bot.player.name} has no room for it.")
            return
        }
        val resume = resumeAfter(bot)
        // done once the count went up (the interaction takes a walk and a tick or two); given up after a while
        val taken = BotOwnedCarrying(id, bot.player.inventory.count(id) + 1)
        val pickup = BotInteractFloorItem("Take", id, delay = 2, success = taken, radius = 1, x = tile.x, y = tile.y)
        val activity = OwnedProfiles.build(owner, bot, slot, PICKUP_PREFIX, listOf(pickup, BotOwnedResume()), timeout = PICKUP_TIMEOUT)
        order(owner, bot, activity, null, OwnedBotStatus.Working, "Picking that up.")
        bot.player["owned_resume"] = resume
    }

    private fun call(owner: Player, slot: Int) {
        val bot = requireBot(owner, slot) ?: return
        if (!BotFollowOwner.recall(bot, owner)) {
            owner.message("${bot.player.name} can't come to you right now.")
            return
        }
        if (!following(bot)) {
            follow(owner, slot)
        }
    }

    private fun order(owner: Player, bot: Bot, activity: BotActivity, task: String?, status: OwnedBotStatus, say: String) {
        manager.stop(bot)
        // A new order ends whatever the bot was doing (following, fighting, a bank errand): actions wait for an empty mode
        bot.player.target = null
        bot.player.mode = EmptyMode
        bot.player.clear("owned_errand")
        bot.player.clear("owned_resume")
        bot.blocked.clear()
        bot.evaluate.clear()
        bot.previous = null
        bot.pin(activity)
        if (task == null) {
            bot.player.clear("owned_task")
        } else {
            bot.player["owned_task"] = task
        }
        setStatus(bot, status)
        bot.player.say(say)
        refresh(owner)
    }

    private fun combat(owner: Player, slot: Int, args: List<String>) {
        val profile = args.getOrNull(0)?.lowercase()
        if (profile == null || profile !in OwnedRoster.PROFILES) {
            owner.message("Which profile? One of ${OwnedRoster.PROFILES.joinToString()}.")
            return
        }
        owner["owned_bot_${slot}_profile"] = profile
        val knobs = OwnedRoster.knobs(owner, slot).toMutableMap()
        for (arg in args.drop(1)) {
            val key = arg.substringBefore('=').lowercase()
            val value = arg.substringAfter('=', "")
            if (key !in KNOBS || value.isBlank()) {
                owner.message("Unknown setting '$arg'. Settings: ${KNOBS.joinToString { "$it=" }}")
                continue
            }
            knobs[key] = value.lowercase()
        }
        OwnedRoster.setKnobs(owner, slot, knobs)
        owner.message("[Bot ${slot + 1}] Combat profile: $profile${if (knobs.isEmpty()) "" else " (${knobs.entries.joinToString { "${it.key}=${it.value}" }})"}")
        val bot = bot(owner, slot) ?: return
        if (following(bot)) {
            follow(owner, slot)
        } else {
            refresh(owner)
        }
    }

    /** The weapon the bot switches to for its special attack when its energy allows, or none; the bot must carry it. */
    private fun spec(owner: Player, slot: Int, item: String?) {
        val knobs = OwnedRoster.knobs(owner, slot).toMutableMap()
        val name = OwnedRoster.name(owner, slot) ?: "Bot ${slot + 1}"
        if (item.equals("none", ignoreCase = true)) {
            knobs.remove("spec")
            owner.message("[Bot ${slot + 1}] $name has no special attack weapon.")
        } else {
            val id = itemId(item) ?: return owner.message("::bot spec <slot|all> <item>|none")
            if (ItemDefinitions.get(id)["special", ""].isEmpty()) {
                owner.message("${id.toItemName().replaceFirstChar { it.uppercase() }} has no special attack.")
                return
            }
            knobs["spec"] = id
            owner.message("[Bot ${slot + 1}] $name's special attack weapon: ${id.toItemName()}.")
        }
        OwnedRoster.setKnobs(owner, slot, knobs)
        val bot = bot(owner, slot) ?: return
        when {
            following(bot) -> follow(owner, slot)
            bot.pinned?.startsWith(HOLD_PREFIX) == true -> hold(owner, slot)
            else -> refresh(owner)
        }
    }

    private fun aggression(owner: Player, slot: Int, mode: String?) {
        val value = mode?.lowercase()
        if (value == null || value !in OwnedRoster.AGGRESSION) {
            owner.message("Aggression is one of ${OwnedRoster.AGGRESSION.joinToString()}.")
            return
        }
        owner["owned_bot_${slot}_aggression"] = value
        bot(owner, slot)?.player?.set("owned_aggression", value)
        owner.message("[Bot ${slot + 1}] Aggression: $value")
        refresh(owner)
    }

    private fun loot(owner: Player, slot: Int, mode: String?) {
        val bot = requireBot(owner, slot) ?: return
        val on = when (mode?.lowercase()) {
            "on" -> true
            "off" -> false
            else -> {
                owner.message("::bot loot <slot> on|off")
                return
            }
        }
        bot.player["owned_loot"] = on
        owner.message("[Bot ${slot + 1}] Looting ${if (on) "on" else "off"}.")
    }

    private fun rename(owner: Player, slot: Int, name: String?) {
        if (OwnedRoster.name(owner, slot) == null || !OwnedRoster.alive(owner, slot)) {
            owner.message("You don't have a bot in slot ${slot + 1}.")
            return
        }
        val wanted = name?.replace('_', ' ')?.trim() ?: return owner.message("::bot rename <slot> <name>")
        val error = OwnedRoster.validateName(wanted, accountDefinitions)
        if (error != null) {
            owner.message(error)
            return
        }
        val price = OwnedRoster.renamePrice()
        if (!owner.inventory.remove("coins", price)) {
            owner.message("Renaming a bot costs ${price.toDigitGroupString()} coins, carried.")
            return
        }
        owner["owned_bot_${slot}_name"] = wanted
        bot(owner, slot)?.player?.name = wanted
        owner.message("[Bot ${slot + 1}] Renamed to $wanted.")
        refresh(owner)
    }

    // ---- items -------------------------------------------------------------------------------------------

    /** What happens once the owner reaches the bot. */
    private class Transfer(val slot: Int, val block: (Player) -> Unit)

    /**
     * Sends the owner over to [bot]; [block] runs beside it. An owner with the bank open leaves it to walk; the
     * engine's interaction reports an unreachable bot.
     */
    private fun approach(owner: Player, bot: Bot, slot: Int, block: (Player) -> Unit) {
        owner.closeInterfaces()
        owner["owned_transfer"] = Transfer(slot, block)
        owner.mode = PlayerOnPlayerInteract(bot.player, TRANSFER, owner)
    }

    private fun equip(owner: Player, slot: Int, item: String?, source: String?) {
        val bot = requireBot(owner, slot) ?: return
        val id = itemId(item) ?: return owner.message("Which item?")
        val player = bot.player
        // Something it already carries it puts on by itself, wherever it is
        if (player.inventory.indexOf(id) != -1) {
            wear(owner, slot, player, id)
            return
        }
        if (source.equals("bank", ignoreCase = true)) {
            // The bot fetches it from the owner's bank itself
            enqueue(owner, bot, slot, BankOrder(BankOrder.Kind.Withdraw, id, 1), BankOrder(BankOrder.Kind.Equip, id))
            return
        }
        if (owner.inventory.indexOf(id) == -1) {
            owner.message("You have no ${id.toItemName()} on you to give.")
            return
        }
        // From the owner's own inventory: handed over in person, then worn
        approach(owner, bot, slot) { target ->
            if (owner.inventory.moveToLimit(id, 1, target.inventory) <= 0) {
                owner.message("No ${id.toItemName()} to equip.")
                return@approach
            }
            wear(owner, slot, target, id)
        }
    }

    private fun wear(owner: Player, slot: Int, player: Player, id: String) {
        val index = player.inventory.indexOf(id)
        val item = player.inventory[index]
        if (index == -1 || !player.hasRequirements(item) || !Equipping.equip(player, item, index)) {
            owner.message("${player.name} can't wear ${id.toItemName()}.")
            return
        }
        owner.message("[Bot ${slot + 1}] ${player.name} wears ${id.toItemName()}.")
    }

    private fun unequip(owner: Player, slot: Int, name: String?) {
        val bot = requireBot(owner, slot) ?: return
        val equip = EquipSlot.entries.firstOrNull { it.index >= 0 && it.name.equals(name, ignoreCase = true) }
        if (equip == null) {
            owner.message("Which slot? ${EquipSlot.entries.filter { it.index >= 0 }.joinToString { it.name.lowercase() }}")
            return
        }
        // Its own hands: no need for the owner to be there
        val player = bot.player
        val item = player.equipment[equip.index]
        if (item.isEmpty()) {
            owner.message("${player.name} has nothing in that slot.")
            return
        }
        if (!player.equipment.move(equip.index, player.inventory)) {
            owner.message("${player.name} has no room to take that off.")
            return
        }
        owner.message("[Bot ${slot + 1}] ${player.name} takes off ${item.id.toItemName()}.")
    }

    /**
     * The bot clicks [option] ("Eat", "Drink", "Drop"...) on what it carries in inventory slot [index], as
     * a player clicking it in their own inventory would: the same instruction, so every item's own handler runs.
     */
    private fun itemOption(owner: Player, slot: Int, index: String?, option: String) {
        val bot = requireBot(owner, slot) ?: return
        val player = bot.player
        val invSlot = index?.toIntOrNull() ?: return owner.message("Which inventory slot? 0 to ${player.inventory.size - 1}.")
        val item = player.inventory.getOrNull(invSlot)
        if (item == null || item.isEmpty()) {
            owner.message("${player.name} has nothing in that slot.")
            return
        }
        val k = item.def.options.indexOfFirst { it.equals(option, ignoreCase = true) }
        if (k !in INVENTORY_OPTIONS.indices) {
            owner.message("${player.name} can't ${option.ifBlank { "do that with" }.lowercase()} ${item.id.toItemName()}.")
            return
        }
        val interfaceId = InterfaceDefinitions.getOrNull("inventory")?.id ?: return
        val componentId = InterfaceDefinitions.getComponentId("inventory", "inventory") ?: return
        if (!instructions.handle(player, InteractInterface(interfaceId, componentId, item.def.id, invSlot, INVENTORY_OPTIONS[k]))) {
            owner.message("${player.name} can't ${option.lowercase()} ${item.id.toItemName()} right now.")
        }
    }

    /** The bot uses the item in inventory slot [from] on the one in slot [to] (tinderbox on logs, etc.). */
    private fun useItem(owner: Player, slot: Int, from: String?, to: String?) {
        val bot = requireBot(owner, slot) ?: return
        val player = bot.player
        val fromSlot = from?.toIntOrNull()
        val toSlot = to?.toIntOrNull()
        val fromItem = fromSlot?.let { player.inventory.getOrNull(it) }
        val toItem = toSlot?.let { player.inventory.getOrNull(it) }
        if (fromSlot == null || toSlot == null || fromItem == null || toItem == null || fromItem.isEmpty() || toItem.isEmpty() || fromSlot == toSlot) {
            owner.message("Use which two of ${player.name}'s items?")
            return
        }
        val interfaceId = InterfaceDefinitions.getOrNull("inventory")?.id ?: return
        val componentId = InterfaceDefinitions.getComponentId("inventory", "inventory") ?: return
        val instruction = InteractInterfaceItem(fromItem.def.id, toItem.def.id, fromSlot, toSlot, interfaceId, componentId, interfaceId, componentId)
        if (!instructions.handle(player, instruction)) {
            owner.message("Nothing interesting happens.")
        }
    }

    private fun give(owner: Player, slot: Int, item: String?, amount: String?, source: String?) {
        val bot = requireBot(owner, slot) ?: return
        val id = itemId(item) ?: return owner.message("Which item?")
        val bank = source.equals("bank", ignoreCase = true) || amount.equals("bank", ignoreCase = true)
        val from = if (bank) owner.bank else owner.inventory
        val available = from.count(id)
        if (available <= 0) {
            owner.message("You have no ${id.toItemName()} ${if (bank) "in your bank" else "on you"}.")
            return
        }
        withAmount(owner, id, amount, available) { count ->
            if (bank) {
                enqueue(owner, bot, slot, BankOrder(BankOrder.Kind.Withdraw, id, count))
                return@withAmount
            }
            approach(owner, bot, slot) { player ->
                val moved = owner.inventory.moveToLimit(id, count, player.inventory)
                if (moved <= 0) {
                    owner.message("No ${id.toItemName()} to give, or ${player.name} has no room.")
                    return@approach
                }
                owner.message("[Bot ${slot + 1}] You give ${player.name} ${if (moved > 1) "$moved x " else ""}${id.toItemName()}.")
            }
        }
    }

    private fun take(owner: Player, slot: Int, item: String?, amount: String?, source: String?) {
        val bot = requireBot(owner, slot) ?: return
        val id = itemId(item) ?: return owner.message("Which item?")
        val bank = source.equals("bank", ignoreCase = true) || amount.equals("bank", ignoreCase = true)
        val available = bot.player.inventory.count(id)
        if (available <= 0) {
            owner.message("${bot.player.name} isn't carrying any ${id.toItemName()}.")
            return
        }
        withAmount(owner, id, amount, available) { count ->
            if (bank) {
                enqueue(owner, bot, slot, BankOrder(BankOrder.Kind.Deposit, id, count))
                return@withAmount
            }
            approach(owner, bot, slot) { player ->
                val moved = player.inventory.moveToLimit(id, count, owner.inventory)
                if (moved <= 0) {
                    owner.message("You have no room to take ${id.toItemName()} from ${player.name}.")
                    return@approach
                }
                owner.message("[Bot ${slot + 1}] You take ${if (moved > 1) "$moved x " else ""}${id.toItemName()} from ${player.name}.")
            }
        }
    }

    private fun bankAll(owner: Player, slot: Int) {
        val bot = requireBot(owner, slot) ?: return
        if (bot.player.inventory.isEmpty()) {
            owner.message("${bot.player.name} has nothing to bank.")
            return
        }
        enqueue(owner, bot, slot, BankOrder(BankOrder.Kind.DepositAll))
    }

    /**
     * [block] with the amount to move: the one given, or, for more than one on hand and none given, the one
     * the owner picks in a dialogue (1, 5, 10, all, or typed).
     */
    private fun withAmount(owner: Player, id: String, amount: String?, available: Int, block: (Int) -> Unit) {
        if (amount.equals("all", ignoreCase = true)) {
            block(available)
            return
        }
        val given = amount?.toIntOrNull()
        if (given != null) {
            block(given.coerceIn(1, available))
            return
        }
        if (available == 1) {
            block(1)
            return
        }
        owner.strongQueue("owned_bot_amount") {
            val count = when (choice(listOf("1", "5", "10", "All ($available)", "X"), "How many ${id.toItemName()}?")) {
                1 -> 1
                2 -> 5
                3 -> 10
                4 -> available
                5 -> intEntry("Enter amount:")
                else -> 0
            }
            if (count > 0) {
                block(count.coerceAtMost(available))
            }
        }
    }

    // ---- loadout presets ---------------------------------------------------------------------------------

    private suspend fun loadout(owner: Player, rest: List<String>) {
        when (rest.getOrNull(0)?.lowercase()) {
            "save" -> {
                // `save <name> [slot]`, or `save <slot>` / `save` with the name asked for; a bare slot number saves that bot's gear
                val first = rest.getOrNull(1)
                val second = rest.getOrNull(2)
                val bareSlot = second == null && first != null && OwnedRoster.parseSlot(first) != null
                val slot = if (bareSlot) OwnedRoster.parseSlot(first) else second?.let { OwnedRoster.parseSlot(it) }
                // No slot: the owner's own worn gear and inventory; a slot: that bot's
                val source = if (slot == null) owner else (requireBot(owner, slot) ?: return).player
                val worn = Loadout.capture(source.equipment, source.inventory)
                if (worn.isEmpty()) {
                    owner.message(if (slot == null) "You have nothing worn or carried to save." else "That bot has nothing worn or carried to save.")
                    return
                }
                if (bareSlot || first == null) {
                    // Dialogues run from the player's queue
                    owner.strongQueue("owned_preset_name") {
                        savePreset(owner, owner.nameEntry("Name the preset:"), worn)
                    }
                } else {
                    savePreset(owner, first, worn)
                }
            }
            "wear", "load" -> withSlot(owner, rest.drop(1)) { slot ->
                val bot = requireBot(owner, slot) ?: return@withSlot
                val name = rest.getOrNull(2)?.lowercase() ?: return@withSlot owner.message("::bot loadout wear <slot> <name>")
                val preset = OwnedLoadouts.get(owner, name) ?: return@withSlot owner.message("No preset called '$name'. ::bot loadout list")
                val orders = load(bot.player, preset)
                if (orders.isEmpty()) {
                    return@withSlot owner.message("${bot.player.name} already has '$name'.")
                }
                enqueue(owner, bot, slot, *orders.toTypedArray())
            }
            "delete" -> {
                val name = rest.getOrNull(1)?.lowercase() ?: return owner.message("::bot loadout delete <name>")
                owner.message(if (OwnedLoadouts.delete(owner, name)) "Preset '$name' deleted." else "No preset called '$name'.")
                listLoadouts(owner)
            }
            else -> listLoadouts(owner)
        }
    }

    private fun savePreset(owner: Player, text: String, worn: Loadout) {
        val name = text.lowercase().replace(' ', '_')
        val error = OwnedLoadouts.validateName(name)
        if (error != null) {
            return owner.message(error)
        }
        if (!OwnedLoadouts.save(owner, name, worn)) {
            return owner.message("You can keep ${OwnedLoadouts.MAX_PRESETS} presets; delete one first.")
        }
        owner.message("Preset '$name' saved: ${worn.describe()}.")
        listLoadouts(owner)
    }

    /** The presets, one line each for the plugin and the owner. */
    private fun listLoadouts(owner: Player) {
        val presets = OwnedLoadouts.all(owner)
        owner.message("[BotLoadouts] ${presets.size} presets" + if (presets.isEmpty()) ": ::bot loadout save <name>" else "")
        for ((name, loadout) in presets) {
            owner.message("[BotLoadouts] $name|${loadout.describe()}")
        }
    }

    /**
     * The bank orders that turn what [player] wears into [worn]: take off (and bank) what the set doesn't have,
     * withdraw and wear what it does. Empty when already dressed so.
     */
    private fun dress(player: Player, worn: Map<EquipSlot, String>): List<BankOrder> {
        val orders = mutableListOf<BankOrder>()
        for (slot in EquipSlot.entries) {
            if (slot.index < 0) {
                continue
            }
            val current = player.equipment[slot.index]
            val wanted = worn[slot]
            if (current.isNotEmpty() && current.id != wanted) {
                orders.add(BankOrder(BankOrder.Kind.TakeOff, slot.name.lowercase()))
            }
        }
        for ((slot, item) in worn) {
            if (player.equipment[slot.index].id == item) {
                continue
            }
            if (player.inventory.indexOf(item) == -1) {
                orders.add(BankOrder(BankOrder.Kind.Withdraw, item, 1))
            }
            orders.add(BankOrder(BankOrder.Kind.Equip, item))
        }
        return orders
    }

    /**
     * The bank orders that give [player] exactly [loadout]: when the carried items differ, bank everything carried
     * and fetch the set's; take off and bank what the set doesn't wear; withdraw and wear the rest. Empty when
     * nothing differs.
     */
    private fun load(player: Player, loadout: Loadout): List<BankOrder> {
        val orders = mutableListOf<BankOrder>()
        val current = Loadout.capture(player.equipment, player.inventory)
        val rebag = current.carried != loadout.carried
        if (rebag) {
            orders.add(BankOrder(BankOrder.Kind.DepositAll))
        }
        for ((slot, item) in current.worn) {
            if (loadout.worn[slot] != item) {
                orders.add(BankOrder(BankOrder.Kind.TakeOff, slot.name.lowercase()))
            }
        }
        for ((slot, item) in loadout.worn) {
            if (current.worn[slot] == item) {
                continue
            }
            if (rebag || player.inventory.indexOf(item) == -1) {
                orders.add(BankOrder(BankOrder.Kind.Withdraw, item, 1))
            }
            orders.add(BankOrder(BankOrder.Kind.Equip, item))
        }
        if (rebag) {
            for ((item, amount) in loadout.carried) {
                orders.add(BankOrder(BankOrder.Kind.Withdraw, item, amount))
            }
        }
        return orders
    }

    // ---- bank errands ------------------------------------------------------------------------------------

    /**
     * Queues bank [orders] on the bot and, unless it is already on its way, sends it to the nearest bank: it
     * uses the booth, does everything queued in one go, and goes back to its previous order ([BotOwnedErrand]).
     */
    private fun enqueue(owner: Player, bot: Bot, slot: Int, vararg orders: BankOrder) {
        val player = bot.player
        val pending = player.getOrPut("owned_errand") { mutableListOf<BankOrder>() }
        pending.addAll(orders)
        if (bot.pinned?.startsWith(ERRAND_PREFIX) == true) {
            owner.message("[Bot ${slot + 1}] ${player.name} will also ${orders.joinToString { it.describe() }}.")
            return
        }
        player["owned_resume"] = bot.pinnedActivity ?: IDLE
        val activity = BotActivity(
            id = ERRAND_PREFIX + player.accountName,
            timeout = ERRAND_TIMEOUT,
            actions = listOf(BotOwnedBankTrip(), BotOpenBank(), BotOwnedErrand()),
        )
        manager.stop(bot)
        player.target = null
        player.mode = EmptyMode
        bot.blocked.clear()
        bot.evaluate.clear()
        bot.previous = null
        bot.pin(activity)
        setStatus(bot, OwnedBotStatus.Banking)
        player.say("Off to the bank.")
        owner.message("[Bot ${slot + 1}] ${player.name} heads to the bank to ${orders.joinToString { it.describe() }}.")
        refresh(owner)
    }

    // ---- crash safety ------------------------------------------------------------------------------------

    private fun snapshotAll() {
        for (bot in manager.bots) {
            val owner = bot.player.owner ?: continue
            owner["owned_bot_${bot.player["owned_slot", 0]}_items"] = encode(bot.player)
        }
    }

    /** `"id:amount"` for everything carried and worn; a list, as the save reader caps single strings at 150 characters. */
    private fun encode(player: Player): List<String> = listOf(player.inventory, player.equipment).flatMap { inventory ->
        inventory.items.filter { it.isNotEmpty() }.map { "${it.id}:${it.amount}" }
    }

    /** Items a bot carried when the server stopped without sweeping go to the bank at the owner's next login. */
    private fun restore(owner: Player) {
        for (slot in 0 until OwnedRoster.MAX_SLOTS) {
            val saved = OwnedRoster.snapshot(owner, slot)
            if (saved.isEmpty() || bot(owner, slot) != null) {
                continue
            }
            var count = 0
            for (entry in saved) {
                val id = entry.substringBefore(':')
                val amount = entry.substringAfter(':', "").toIntOrNull() ?: continue
                if (amount <= 0 || !ItemDefinitions.contains(id)) {
                    continue
                }
                val item = Item(id, amount)
                val unnoted = if (item.isNote) item.noted ?: item else item
                var added = 0
                owner.bank.transaction { added = addToLimit(unnoted.id, amount) }
                if (amount - added > 0) {
                    owner.addOrDrop(unnoted.id, amount - added)
                }
                count++
            }
            owner.clear("owned_bot_${slot}_items")
            if (count > 0) {
                notify(owner, slot, "Recovered the items ${OwnedRoster.name(owner, slot) ?: "your bot"} was carrying.")
            }
        }
    }

    // ---- state -------------------------------------------------------------------------------------------

    private fun sync() {
        for (bot in manager.bots) {
            val owner = bot.player.owner ?: continue
            val slot = bot.player["owned_slot", -1]
            if (slot < 0) {
                continue
            }
            restrict(owner, bot, slot)
            derive(bot)
            if (OwnedBotVarcs.active(owner)) {
                OwnedBotVarcs.stats(owner, slot, bot)
                OwnedBotVarcs.order(owner, slot, bot, tasks)
            }
        }
    }

    /**
     * Bots keep out of PvP: a bot that finds itself in the wilderness or a PvP area drops its order and, when it
     * can, teleports back to its owner. Closes the "kill your own bot to move its gear" route.
     */
    private fun restrict(owner: Player, bot: Bot, slot: Int) {
        val player = bot.player
        if (!player.inWilderness && !player.inPvp) {
            return
        }
        if (bot.pinned != IDLE.id) {
            bot.pin(IDLE)
            bot.player.clear("owned_task")
            bot.player.clear("owned_errand")
            bot.player.clear("owned_resume")
            manager.stop(bot)
            setStatus(bot, OwnedBotStatus.Failed)
            notify(owner, slot, "${player.name} won't go into a PvP area.")
        }
        if (!owner.inWilderness && !owner.inPvp) {
            BotFollowOwner.recall(bot, owner)
        }
    }

    /** Working/banking/following/fighting from what the bot is doing; sticky statuses stay until the next order. */
    private fun derive(bot: Bot) {
        val current = OwnedBotStatus.entries[bot.player["owned_status", OwnedBotStatus.Idle.ordinal]]
        if (bot.pinned == FLEE.id && bot.lastSuccessful?.id == FLEE.id) {
            // Reached the bank after fleeing: stand down
            bot.pin(IDLE)
            setStatus(bot, OwnedBotStatus.Idle)
            return
        }
        if (current in STICKY) {
            return
        }
        val status = when {
            bot.pinned == IDLE.id -> OwnedBotStatus.Idle
            bot.pinned?.startsWith(ERRAND_PREFIX) == true -> OwnedBotStatus.Banking
            bot.frames.any { it.behaviour.id in OwnedResolvers.ids && it.behaviour.id != "go_to_area" } -> OwnedBotStatus.Banking
            following(bot) -> if (bot.player.attacking) OwnedBotStatus.Fighting else OwnedBotStatus.Following
            bot.pinned?.startsWith(HOLD_PREFIX) == true -> if (bot.player.attacking) OwnedBotStatus.Fighting else OwnedBotStatus.Guarding
            bot.pinned?.startsWith(ATTACK_PREFIX) == true -> OwnedBotStatus.Fighting
            else -> OwnedBotStatus.Working
        }
        if (status != current) {
            setStatus(bot, status)
        }
    }

    private fun failed(bot: Bot, behaviour: Behaviour, reason: Reason) {
        if (!bot.player.contains("owner") || reason !is HardReason || reason is Reason.Cancelled) {
            return
        }
        val owner = bot.player.owner ?: return
        val slot = bot.player["owned_slot", -1]
        if (bot.pinned == IDLE.id) {
            return
        }
        if (behaviour.id.startsWith(ERRAND_PREFIX) || bot.pinned?.startsWith(ERRAND_PREFIX) == true) {
            // Couldn't get to a bank: drop the orders and go back to what it was doing
            bot.player.clear("owned_errand")
            val resume = bot.player.get<BotActivity>("owned_resume")
            bot.player.clear("owned_resume")
            bot.pin(resume ?: IDLE)
            notify(
                owner,
                slot,
                "${bot.player.name} couldn't get to a bank: ${when (reason) {
                    Reason.NoRoute -> "it can't find a way there."
                    is Reason.Invalid -> reason.message
                    else -> "it made no progress."
                }}",
            )
            setStatus(bot, if (bot.pinned == IDLE.id) OwnedBotStatus.Idle else OwnedBotStatus.Working)
            refresh(owner)
            return
        }
        if (reason is Reason.Escaped) {
            // BotEscape has told the owner; with no teleport the bot runs for the nearest bank
            bot.player.clear("owned_task")
            setStatus(bot, OwnedBotStatus.Fled)
            if (bot.player[BotEscape.RUN, false]) {
                bot.player.clear(BotEscape.RUN)
                bot.pin(FLEE)
            } else {
                bot.pin(IDLE)
            }
            refresh(owner)
            return
        }
        val detail = when (reason) {
            is Reason.Requirement -> requirementText(bot.player, reason.condition)
            is Reason.Invalid -> reason.message
            Reason.Timeout -> "it made no progress for a while."
            Reason.NoRoute -> "it can't find a way there."
            else -> reason.toString()
        }
        bot.pin(IDLE)
        bot.player.clear("owned_task")
        setStatus(bot, OwnedBotStatus.Failed)
        bot.player.say("I can't carry on with that.")
        notify(owner, slot, "${bot.player.name} stopped: $detail")
        refresh(owner)
    }

    private fun requirementText(player: Player, condition: Condition): String = when (condition) {
        is BotSkillLevel -> "it needs level ${condition.min ?: 1} ${condition.skill.name}."
        is BotMembers -> "that is for members only."
        is BotCombatLevel -> "it needs combat level ${condition.min ?: 1}."
        else -> {
            val missing = OwnedResolvers.missing(player, condition)
            if (missing.isEmpty()) "it needs $condition." else "it needs ${missing.joinToString { it.toItemName() }} in your bank."
        }
    }

    private fun itemsChanged(player: Player) {
        if (!player.contains("owner")) {
            return
        }
        val owner = player.owner ?: return
        OwnedBotVarcs.items(owner, player["owned_slot", 0], player.bot)
    }

    private fun refresh(owner: Player) {
        OwnedBotVarcs.showAll(owner, slots(owner), tasks)
    }

    private fun setStatus(bot: Bot, status: OwnedBotStatus) {
        bot.player["owned_status"] = status.ordinal
    }

    private fun notify(owner: Player, slot: Int, text: String) {
        owner.message("[Bot ${slot + 1}] $text")
    }

    // ---- admin -------------------------------------------------------------------------------------------

    private fun admin(admin: Player, args: List<String>) {
        val action = args.getOrNull(0)?.lowercase() ?: "list"
        val target = args.getOrNull(1)?.let { Players.find(it) ?: Players.findByAccount(it) }
        val slot = args.getOrNull(2)?.let { OwnedRoster.parseSlot(it) }
        when (action) {
            "list" -> {
                var count = 0
                for (bot in manager.bots) {
                    val account = bot.player.get<String>("owner") ?: continue
                    count++
                    admin.message("$account [${bot.player["owned_slot", 0] + 1}] ${bot.player.name} ${bot.player.tile} ${bot.pinned} ${OwnedBotStatus.entries[bot.player["owned_status", 0]]}")
                }
                admin.message("$count owned bots online.")
            }
            "inspect" -> {
                if (target == null) {
                    return admin.message("::owned_bots inspect <player>")
                }
                for (index in 0 until OwnedRoster.slots()) {
                    admin.message("[${index + 1}] ${OwnedRoster.name(target, index) ?: "-"} alive=${OwnedRoster.alive(target, index)} online=${bot(target, index) != null} profile=${OwnedRoster.profile(target, index)} snapshot=${OwnedRoster.snapshot(target, index).size}")
                }
            }
            "dismiss" -> {
                if (target == null || slot == null) {
                    return admin.message("::owned_bots dismiss <player> <slot>")
                }
                dismiss(target, slot)
                admin.message("Dismissed ${target.name}'s bot ${slot + 1}.")
            }
            "refund" -> {
                if (target == null || slot == null) {
                    return admin.message("::owned_bots refund <player> <slot>")
                }
                if (OwnedRoster.name(target, slot) == null) {
                    return admin.message("${target.name} never had a bot in slot ${slot + 1}.")
                }
                target["owned_bot_${slot}_alive"] = true
                refresh(target)
                admin.message("${target.name}'s bot ${slot + 1} is alive again.")
            }
            "grant" -> {
                val name = args.getOrNull(3)?.replace('_', ' ')
                if (target == null || slot == null || name == null) {
                    return admin.message("::owned_bots grant <player> <slot> <name>")
                }
                if (OwnedRoster.alive(target, slot)) {
                    return admin.message("${target.name} already has a living bot in slot ${slot + 1}.")
                }
                val error = OwnedRoster.validateName(name, accountDefinitions)
                if (error != null) {
                    return admin.message(error)
                }
                OwnedRoster.purchase(target, slot, name)
                refresh(target)
                admin.message("${target.name} now owns $name in slot ${slot + 1} (no charge).")
            }
            else -> admin.message("::owned_bots list|inspect <player>|dismiss <player> <slot>|refund <player> <slot>|grant <player> <slot> <name>")
        }
    }

    // ---- helpers -----------------------------------------------------------------------------------------

    private fun enabled(): Boolean = Settings["bots.owned.enabled", false]

    private fun following(bot: Bot): Boolean = bot.pinned?.startsWith(FOLLOW_PREFIX) == true

    private fun slots(owner: Player): Array<Bot?> = Array(OwnedRoster.MAX_SLOTS) { ownedBot(owner, it) }

    private fun bots(owner: Player): List<Bot> = slots(owner).filterNotNull()

    private fun bot(owner: Player, slot: Int): Bot? = ownedBot(owner, slot)

    private fun liveCount(): Int = manager.bots.count { it.player.contains("owner") }

    private fun requireBot(owner: Player, slot: Int): Bot? {
        val bot = bot(owner, slot)
        if (bot == null) {
            val name = OwnedRoster.name(owner, slot)
            owner.message(if (name == null || !OwnedRoster.alive(owner, slot)) "You don't have a bot in slot ${slot + 1}." else "$name isn't with you. ::bot summon ${slot + 1}")
        }
        return bot
    }

    /** An item by its cache id (what the plugin sends: no name to mangle) or its string id. */
    private fun itemId(text: String?): String? {
        val trimmed = text?.trim() ?: return null
        val number = trimmed.toIntOrNull()
        if (number != null) {
            return ItemDefinitions.getOrNull(number)?.stringId
        }
        val id = trimmed.lowercase().replace(' ', '_')
        return if (ItemDefinitions.contains(id)) id else null
    }

    private fun String.toItemName(): String = replace('_', ' ')

    companion object {
        /** Ids of the per-bot bank errand activities. */
        const val ERRAND_PREFIX = "owned_errand:"

        /** Ticks an errand may take without progress before it is given up (a long walk to a bank). */
        const val ERRAND_TIMEOUT = 1500

        /** How far apart two halls' Adventurers are at least: the four of one hall count as one hall's. */
        private const val HALL_RADIUS = 12

        /** The hidden player option the owner uses to reach a bot for give/take/equip. */
        const val TRANSFER = "Bot items"

        /** Sent by the plugin in `::bot ui`; not typeable in the chat box or console. */
        const val HANDSHAKE = '\u0001'

        val ACTIONS = setOf("help", "ui", "tasks", "status", "summon", "dismiss", "call", "task", "follow", "stop", "guard", "hold", "goto", "attack", "pickup", "combat", "aggression", "equip", "unequip", "give", "take", "bank", "item", "use", "loot", "rename", "show", "loadout")

        val KNOBS = setOf("spec", "eat", "flee", "warn", "escape")

        /** The instruction's option for each of an item's five inventory options: what clicking op 1-5 in the inventory (149:0) sends. */
        val INVENTORY_OPTIONS = intArrayOf(0, 1, 2, 6, 7)

        /** Owner settings a bot keeps in step with. */
        val MIRRORED = listOf("auto_retaliate", "spellbook", "prayers")

        private val STICKY = setOf(OwnedBotStatus.Low, OwnedBotStatus.Fled, OwnedBotStatus.Failed, OwnedBotStatus.Dead)

        /** Standing by: a pinned activity that does nothing, so the manager never picks a world activity. */
        val IDLE = BotActivity("owned_idle", timeout = Int.MAX_VALUE, actions = listOf(BotWait(10)))

        /** Ids of the per-bot follow activities built by [OwnedProfiles]. */
        const val FOLLOW_PREFIX = "owned_follow:"
        const val HOLD_PREFIX = "owned_hold:"
        const val GOTO_PREFIX = "owned_goto:"
        const val ATTACK_PREFIX = "owned_attack:"
        const val PICKUP_PREFIX = "owned_pickup:"

        /** How far from the owner a bot can be sent, and how far from the bot a target or an item can be. */
        const val GOTO_RANGE = 30
        const val ATTACK_RANGE = 30

        /** Ticks a walk or a pick-up gets before the order is given up as failed. */
        const val GOTO_TIMEOUT = 100
        const val PICKUP_TIMEOUT = 50

        /** A backstop for a fight (reachability is checked when the order is given): five minutes on one target. */
        const val ATTACK_TIMEOUT = 500

        /** Reachability and line of sight, as the world bots see them. */
        private val world = BotGameWorld()

        /** No teleport out of a lost fight: run to the nearest bank and stand down. */
        val FLEE = BotActivity("owned_flee", timeout = 500, actions = listOf(BotGoToNearest("bank"), BotWait(5)))

        fun accountName(owner: Player, slot: Int): String = "${owner.accountName}#bot${slot + 1}"
    }
}

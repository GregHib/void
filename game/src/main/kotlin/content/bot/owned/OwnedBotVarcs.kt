package content.bot.owned

import content.area.wilderness.inPvp
import content.area.wilderness.inWilderness
import content.bot.Bot
import content.bot.behaviour.action.BotOwnedSpell
import content.entity.player.effect.energy.MAX_RUN_ENERGY
import content.entity.player.effect.energy.runEnergy
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory

/**
 * What the owner's 634 client plugin sees of their bots: client variables from `owned.varcs.toml`, ints only
 * (the Void client does not extend varcstrings past the cache). Sent only once the plugin has announced itself
 * with `::bot ui` (see [OwnedBots]); a vanilla client would crash on ids past its table.
 */
object OwnedBotVarcs {
    const val VERSION = 1
    const val NO_TASK = 255
    const val EQUIPMENT_SLOTS = 14
    const val INVENTORY_SLOTS = 28

    fun active(owner: Player): Boolean = owner["bot_ui", false]

    /** Everything, for the handshake and after summon/dismiss. */
    fun showAll(owner: Player, live: Array<Bot?>, tasks: OwnedTasks) {
        if (!active(owner)) {
            return
        }
        meta(owner, live)
        for (slot in 0 until OwnedRoster.MAX_SLOTS) {
            val bot = live.getOrNull(slot)
            name(owner, slot)
            order(owner, slot, bot, tasks)
            stats(owner, slot, bot)
            items(owner, slot, bot)
        }
    }

    fun meta(owner: Player, live: Array<Bot?>) {
        if (!active(owner)) {
            return
        }
        var alive = 0
        var online = 0
        for (slot in 0 until OwnedRoster.MAX_SLOTS) {
            if (OwnedRoster.alive(owner, slot)) {
                alive = alive or (1 shl slot)
            }
            if (live.getOrNull(slot) != null) {
                online = online or (1 shl slot)
            }
        }
        owner["owned_bots_meta"] = (VERSION shl 12) or (alive shl 4) or online
    }

    fun name(owner: Player, slot: Int) {
        if (!active(owner)) {
            return
        }
        val name = OwnedRoster.name(owner, slot)
        val (hi, lo) = if (name == null || !OwnedRoster.alive(owner, slot)) 0 to 0 else base37(name)
        owner["owned_bot_${slot}_name_hi"] = hi
        owner["owned_bot_${slot}_name_lo"] = lo
    }

    /** Task index, profile and aggression, plus the status code. */
    fun order(owner: Player, slot: Int, bot: Bot?, tasks: OwnedTasks) {
        if (!active(owner)) {
            return
        }
        val task = bot?.player?.get<String>("owned_task")?.let { tasks.get(it)?.index } ?: NO_TASK
        val profile = OwnedRoster.PROFILES.indexOf(OwnedRoster.profile(owner, slot)).coerceAtLeast(0)
        val aggression = OwnedRoster.AGGRESSION.indexOf(OwnedRoster.aggression(owner, slot)).coerceAtLeast(0)
        owner["owned_bot_${slot}_task"] = (aggression shl 12) or (profile shl 8) or task
        // the status in the low byte, the component id of the spell it autocasts above it, and which book that is
        // (0 modern, 1 ancient, 2 lunar) in the top bits
        val status = bot?.player?.get("owned_status", OwnedBotStatus.Idle.ordinal)
            ?: if (OwnedRoster.alive(owner, slot)) OwnedBotStatus.None.ordinal else OwnedBotStatus.Dead.ordinal
        val spell = bot?.player?.get<String>("autocast_spell")
        val book = spell?.let { BotOwnedSpell.bookOf(it) }
        val component = if (spell == null || book == null) 0 else InterfaceDefinitions.getComponentId(book, spell) ?: 0
        val bookIndex = BotOwnedSpell.BOOKS.indexOf(book).coerceAtLeast(0)
        owner["owned_bot_${slot}_status"] = (bookIndex shl 24) or (component shl 8) or status
    }

    /** Hitpoints, prayer, tile and run energy: the per-tick part. */
    fun stats(owner: Player, slot: Int, bot: Bot?) {
        if (!active(owner)) {
            return
        }
        val player = bot?.player
        if (player == null) {
            owner["owned_bot_${slot}_hp"] = 0
            owner["owned_bot_${slot}_prayer"] = 0
            owner["owned_bot_${slot}_tile"] = -1
            owner["owned_bot_${slot}_energy"] = 0
            return
        }
        val hidden = player.inWilderness || player.inPvp
        owner["owned_bot_${slot}_hp"] = (player.levels.get(Skill.Constitution) shl 16) or player.levels.getMax(Skill.Constitution)
        owner["owned_bot_${slot}_prayer"] = (player.levels.get(Skill.Prayer) shl 16) or player.levels.getMax(Skill.Prayer)
        owner["owned_bot_${slot}_tile"] = if (hidden) -1 else pack(player.tile.level, player.tile.x, player.tile.y)
        // run energy (0-100) in the low byte, the special attack weapon's item id above it
        val spec = OwnedRoster.knobs(owner, slot)["spec"]?.let { ItemDefinitions.getOrNull(it)?.id } ?: 0
        owner["owned_bot_${slot}_energy"] = (spec shl 8) or (player.runEnergy * 100 / MAX_RUN_ENERGY)
    }

    /** Worn and carried items, on change. */
    fun items(owner: Player, slot: Int, bot: Bot?) {
        if (!active(owner)) {
            return
        }
        val player = bot?.player
        for (index in 0 until EQUIPMENT_SLOTS) {
            owner["owned_bot_${slot}_equip_$index"] = item(player?.equipment?.getOrNull(index))
        }
        for (index in 0 until INVENTORY_SLOTS) {
            owner["owned_bot_${slot}_inv_$index"] = item(player?.inventory?.getOrNull(index))
        }
    }

    fun clear(owner: Player) {
        owner["owned_bots_meta"] = 0
        for (slot in 0 until OwnedRoster.MAX_SLOTS) {
            owner["owned_bot_${slot}_name_hi"] = 0
            owner["owned_bot_${slot}_name_lo"] = 0
            owner["owned_bot_${slot}_task"] = 0
            owner["owned_bot_${slot}_status"] = 0
            owner["owned_bot_${slot}_hp"] = 0
            owner["owned_bot_${slot}_prayer"] = 0
            owner["owned_bot_${slot}_tile"] = 0
            owner["owned_bot_${slot}_energy"] = 0
            for (index in 0 until EQUIPMENT_SLOTS) {
                owner["owned_bot_${slot}_equip_$index"] = 0
            }
            for (index in 0 until INVENTORY_SLOTS) {
                owner["owned_bot_${slot}_inv_$index"] = 0
            }
        }
    }

    fun item(item: Item?): Int = if (item == null || item.isEmpty()) 0 else (item.def.id shl 16) or item.amount.coerceIn(0, 0xFFFF)

    fun pack(level: Int, x: Int, y: Int): Int = level shl 28 or (x shl 14) or y

    /** RuneScape's name encoding: up to 12 of a-z, 0-9 and _ (a space), as a long split into two ints. */
    fun base37(name: String): Pair<Int, Int> {
        var value = 0L
        for (char in name.lowercase().take(12)) {
            value *= 37
            value += when (char) {
                in 'a'..'z' -> char - 'a' + 1
                in '0'..'9' -> char - '0' + 27
                else -> 0
            }
        }
        return (value ushr 32).toInt() to value.toInt()
    }
}

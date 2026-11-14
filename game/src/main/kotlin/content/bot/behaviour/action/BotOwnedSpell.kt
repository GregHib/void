package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import content.bot.owned.owner
import content.skill.magic.spell.hasSpellItems
import content.skill.magic.spell.spellBook
import content.skill.melee.weapon.attackRange
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.menu
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name

/**
 * Reactive: keeps an owned bot's autocast on [spell], the owner's pick for it (a component of a spellbook, such
 * as `fire_strike` or `ice_barrage`), or with none picked and [mirror] set, on whatever its owner is autocasting.
 * The bot opens whichever book the spell is in; the owner's own book is never asked of it, only the level and the
 * runes. The engine drops an autocast when the runes run out or the weapon changes; it is put back, and when it
 * keeps being dropped (several times in a few ticks: no runes) the owner is told and it is left for a while.
 */
data class BotOwnedSpell(val spell: String?, val mirror: Boolean) : BotAction {

    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState {
        val player = bot.player
        if (spell == null) {
            if (mirror) {
                mirror(player)
            }
            return BehaviourState.Success
        }
        val book = bookOf(spell) ?: return BehaviourState.Success
        if (player.spellBook != book && (player.menu != null || !player.open(book))) {
            return BehaviourState.Success
        }
        val cast = castId(player, spell) ?: return BehaviourState.Success
        if (!player.hasSpellItems(spell, message = false)) {
            // no runes for it: an autocast left set would have every swing refused, and the refusal ends the
            // fight, so the bot fights with its weapon instead and the owner hears once in a while
            if (player["autocast", 0] == cast) {
                clear(player)
            }
            if (!player.hasClock(BACKOFF)) {
                player.owner?.message("[Companion ${player["owned_slot", 0] + 1}] ${player.name} is out of runes for ${spell.replace('_', ' ')}.")
                player.start(BACKOFF, BACKOFF_TICKS)
            }
            return BehaviourState.Success
        }
        if (player["autocast", 0] == cast || player.hasClock(BACKOFF)) {
            return BehaviourState.Success
        }
        val drops = if (player.hasClock(WINDOW)) player[DROPS, 0] + 1 else 1
        if (!player.hasClock(WINDOW)) {
            player.start(WINDOW, WINDOW_TICKS)
        }
        player[DROPS] = drops
        if (drops > MAX_DROPS) {
            player.owner?.message("[Companion ${player["owned_slot", 0] + 1}] ${player.name} is out of runes for ${spell.replace('_', ' ')}.")
            player.start(BACKOFF, BACKOFF_TICKS)
            player.clear(DROPS)
            return BehaviourState.Success
        }
        set(player, spell, cast)
        return BehaviourState.Success
    }

    private fun mirror(player: Player) {
        val owner = player.owner ?: return
        val cast = owner["autocast", 0]
        if (cast == 0) {
            if (player[MIRRORED, false]) {
                player.clear(MIRRORED)
                player.clear("autocast")
            }
            return
        }
        val spell: String = owner["autocast_spell", ""]
        if (!player.hasSpellItems(spell, message = false)) {
            // the owner's spell without the runes for it: fights with its weapon, as with a chosen spell
            if (player["autocast", 0] == cast) {
                clear(player)
            }
            return
        }
        if (player["autocast", 0] == cast) {
            return
        }
        val book = bookOf(spell) ?: return
        if (player.spellBook != book && (player.menu != null || !player.open(book))) {
            return
        }
        if (castId(player, spell) != cast) {
            return
        }
        set(player, spell, cast)
        player[MIRRORED] = true
    }

    companion object {
        private const val WINDOW = "owned_spell_window"
        private const val BACKOFF = "owned_spell_backoff"
        private const val DROPS = "owned_spell_drops"
        private const val MIRRORED = "owned_spell_mirrored"
        private const val WINDOW_TICKS = 20
        private const val BACKOFF_TICKS = 100
        private const val MAX_DROPS = 3

        val BOOKS = listOf("modern_spellbook", "ancient_spellbook", "lunar_spellbook")

        /** The book with a combat spell called [spell], or null. */
        fun bookOf(spell: String): String? = BOOKS.firstOrNull { InterfaceDefinitions.getComponent(it, spell)?.getOrNull<Int>("cast_id") != null }

        /** The cast id of [spell] in [player]'s current book; null when it isn't there or isn't a combat spell. */
        fun castId(player: Player, spell: String): Int? = InterfaceDefinitions.getComponent(player.spellBook, spell)?.getOrNull("cast_id")

        /** What the game's own Autocast option does. */
        fun set(player: Player, spell: String, cast: Int) {
            player["autocast_spell"] = spell
            player.attackRange = 8
            player["autocast"] = cast
        }

        fun clear(player: Player) {
            player.clear(MIRRORED)
            player.clear("autocast")
        }
    }
}

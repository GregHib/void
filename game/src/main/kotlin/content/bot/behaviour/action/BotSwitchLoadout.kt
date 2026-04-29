package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import content.bot.behaviour.activity.BotActivity
import content.bot.behaviour.condition.Condition
import world.gregs.voidps.engine.GameLoop

/**
 * Reactive action for hybrid PvP bots: swap the entire worn kit to a named loadout declared on
 * the bot's current [BotActivity]. Two safety rails:
 * 1. Change-trigger — short-circuits when the bot is already on (or transitioning into) the target.
 * 2. Cooldown — stamps `last_loadout_swap_tick` and gates new swaps for [BotActivity.hybridSwapCooldown] ticks.
 *
 * Target resolution:
 * - Explicit: `to = "magic"` always picks the named loadout.
 * - Auto-counter: `counter_attacker = true` reads `incomingAttackStyle` and picks the counter from
 *   the OSRS combat triangle (melee→magic, ranged→melee, magic→ranged). Loadout names must be
 *   "melee" / "ranged" / "magic" for this to resolve. If the counter loadout isn't declared on
 *   this activity (e.g. a melee/magic-only hybrid hit by magic looks up "ranged"), falls back
 *   to the "magic" loadout so the bot still moves to a defensive kit.
 *
 * Once the rails clear, delegates to [BotSwitchSetup] for the per-slot equip (up to
 * [BotActivity.hybridSwapPerTick] slots committed per tick). When the target loadout declares an
 * `autocast` spell, that autocast is bound on entry so subsequent attacks fire the spell.
 */
data class BotSwitchLoadout(
    val to: String? = null,
    val counterAttacker: Boolean = false,
    val condition: Condition? = null,
) : BotAction {
    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState {
        val player = bot.player
        if (condition != null && !condition.check(player)) return BehaviourState.Success

        val activity = frame.behaviour as? BotActivity ?: return BehaviourState.Success

        val resolved = if (counterAttacker) {
            val style = bot.combatContext?.incomingAttackStyle ?: return BehaviourState.Success
            val counter = COUNTERS[style] ?: return BehaviourState.Success
            // If the counter loadout isn't declared on this activity, fall back to "magic"
            // (better defensive coverage than staying in the wrong kit). Explicit `to` lookups
            // are not subject to this fallback — they should fail loudly if misconfigured.
            if (activity.loadouts.containsKey(counter)) counter else COUNTER_FALLBACK
        } else {
            to ?: return BehaviourState.Success
        }

        val target = activity.loadouts[resolved] ?: return BehaviourState.Success

        val current = player["current_loadout", activity.hybridStartingLoadout ?: ""]
        if (current == resolved) {
            ensureAutocast(player, target.autocast)
            return BotSwitchSetup(target.equipment.items, condition = null, maxPerTick = activity.hybridSwapPerTick).update(bot, world, frame)
        }

        val last = player["last_loadout_swap_tick", -10_000]
        if (GameLoop.tick - last <= activity.hybridSwapCooldown) return BehaviourState.Success

        player["current_loadout"] = resolved
        player["last_loadout_swap_tick"] = GameLoop.tick
        ensureAutocast(player, target.autocast)

        return BotSwitchSetup(target.equipment.items, condition = null, maxPerTick = activity.hybridSwapPerTick).update(bot, world, frame)
    }

    companion object {
        // Combat triangle: each style is countered by the one that beats it.
        // melee > ranged > magic > melee (cycle). Bot picks counter to attacker's style.
        private val COUNTERS = mapOf(
            "melee" to "magic",
            "ranged" to "melee",
            "magic" to "ranged",
        )
        private const val COUNTER_FALLBACK = "melee"
    }
}

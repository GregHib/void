package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import content.bot.behaviour.activity.BotActivity
import content.bot.behaviour.condition.Condition
import content.bot.behaviour.loadout.Loadout
import content.bot.behaviour.loadout.LoadoutDefinitions
import world.gregs.voidps.engine.GameLoop

/**
 * Reactive action for hybrid PvP bots: swap the worn kit between the activity's bound [Loadout]
 * and the loadouts it lists in [Loadout.swaps]. Two safety rails:
 * 1. Change-trigger — short-circuits when the bot is already on (or transitioning into) the target.
 * 2. Cooldown — stamps `last_loadout_swap_tick` and gates new swaps for [cooldown] ticks.
 *
 * Target resolution:
 * - Explicit: `to = "pvp_dangerous_hybrid_magic"` always picks the loadout with that id.
 * - Auto-counter: `counter_attacker = true` reads `incomingAttackStyle` and picks the loadout whose
 *   [Loadout.style] counters it from the OSRS combat triangle (melee→magic, ranged→melee, magic→ranged).
 *   If there's no loadout for the counter style (e.g. a melee/magic-only hybrid hit by magic looks
 *   for "ranged"), falls back to the "melee" loadout so the bot still moves to a defensive kit.
 *
 * Once the rails clear, delegates to [BotSwitchSetup] for the per-slot equip (up to
 * [swapsPerTick] slots committed per tick). When the target loadout declares an
 * `autocast` spell, that autocast is bound on entry so subsequent attacks fire the spell.
 */
data class BotSwitchLoadout(
    val to: String? = null,
    val counterAttacker: Boolean = false,
    val condition: Condition? = null,
    val cooldown: Int = 3,
    val swapsPerTick: Int = 1,
) : BotAction {
    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState {
        val player = bot.player
        if (condition != null && !condition.check(player)) return BehaviourState.Success

        val activity = frame.behaviour as? BotActivity ?: return BehaviourState.Success
        val bound = LoadoutDefinitions.getOrNull(activity.boundLoadout ?: return BehaviourState.Success) ?: return BehaviourState.Success
        if (bound.swaps.isEmpty()) return BehaviourState.Success
        val options = listOf(bound) + bound.swaps.map(LoadoutDefinitions::get)

        val target = if (counterAttacker) {
            val style = bot.combatContext?.incomingAttackStyle ?: return BehaviourState.Success
            val counter = COUNTERS[style] ?: return BehaviourState.Success
            options.firstOrNull { it.style == counter } ?: options.firstOrNull { it.style == COUNTER_FALLBACK } ?: return BehaviourState.Success
        } else {
            options.firstOrNull { it.id == to } ?: return BehaviourState.Success
        }
        val resolved = target.id

        val current = player["current_loadout", bound.id]
        if (current == resolved) {
            ensureAutocast(player, target.autocast)
            return BotSwitchSetup(target.equipment.items, condition = null, maxPerTick = swapsPerTick).update(bot, world, frame)
        }

        val last = player["last_loadout_swap_tick", -10_000]
        if (GameLoop.tick - last <= cooldown) return BehaviourState.Success

        player["current_loadout"] = resolved
        player["last_loadout_swap_tick"] = GameLoop.tick
        ensureAutocast(player, target.autocast)

        return BotSwitchSetup(target.equipment.items, condition = null, maxPerTick = swapsPerTick).update(bot, world, frame)
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

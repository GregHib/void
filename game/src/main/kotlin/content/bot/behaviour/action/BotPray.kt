package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import content.bot.behaviour.Reason
import content.bot.behaviour.condition.Condition
import content.skill.prayer.PrayerConfigs
import content.skill.prayer.getActivePrayerVarKey
import content.skill.prayer.isCurses
import world.gregs.voidps.engine.data.definition.PrayerDefinitions
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasMax
import world.gregs.voidps.engine.get

data class BotPray(val id: String, val condition: Condition? = null) : BotAction {
    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState {
        val player = bot.player
        val shouldBeOn = condition?.check(player) ?: true
        if (!shouldBeOn) {
            val key = player.getActivePrayerVarKey()
            if (player.containsVarbit(key, id)) {
                player.removeVarbit(key, id)
            }
            return BehaviourState.Success
        }
        val def = get<PrayerDefinitions>().getOrNull(id)
            ?: return BehaviourState.Failed(Reason.Invalid("Unknown prayer '$id'."))
        // Auto-switch the prayer book so curse ids land in ACTIVE_CURSES (and vice versa);
        // BotPray bypasses the normal toggle-by-component path that would have flipped the book.
        if (def.isCurse && !player.isCurses()) {
            player[PrayerConfigs.PRAYERS] = "curses"
        } else if (!def.isCurse && player.isCurses()) {
            player[PrayerConfigs.PRAYERS] = "normal"
        }
        val key = player.getActivePrayerVarKey()
        if (player.containsVarbit(key, id)) {
            return BehaviourState.Success
        }
        if (!player.hasMax(Skill.Prayer, def.level)) {
            return BehaviourState.Failed(Reason.Invalid("Insufficient prayer level for '$id': need ${def.level}."))
        }
        if (!player.has(Skill.Prayer, 1)) {
            // Out of prayer points is transient — drink_potion will top up. Don't surface as
            // Failed; the reactive will retry next tick once the bot has sipped a super_restore.
            return BehaviourState.Success
        }
        player.addVarbit(key, id)
        return BehaviourState.Success
    }
}

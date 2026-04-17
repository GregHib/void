package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import content.bot.behaviour.Reason
import content.bot.behaviour.condition.Condition
import content.skill.prayer.getActivePrayerVarKey
import world.gregs.voidps.engine.data.definition.PrayerDefinitions
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasMax
import world.gregs.voidps.engine.get

data class BotPray(val id: String, val condition: Condition? = null) : BotAction {
    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState {
        val player = bot.player
        val key = player.getActivePrayerVarKey()
        val active = player.containsVarbit(key, id)
        val shouldBeOn = condition?.check(player) ?: true
        if (!shouldBeOn) {
            if (active) {
                player.removeVarbit(key, id)
            }
            return BehaviourState.Success
        }
        if (active) {
            return BehaviourState.Success
        }
        val def = get<PrayerDefinitions>().getOrNull(id)
            ?: return BehaviourState.Failed(Reason.Invalid("Unknown prayer '$id'."))
        if (!player.hasMax(Skill.Prayer, def.level)) {
            return BehaviourState.Failed(Reason.Invalid("Insufficient prayer level for '$id': need ${def.level}."))
        }
        if (!player.has(Skill.Prayer, 1)) {
            return if (condition != null) BehaviourState.Success else BehaviourState.Failed(Reason.Invalid("No prayer points to activate '$id'."))
        }
        player.addVarbit(key, id)
        return BehaviourState.Success
    }
}

package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import content.bot.behaviour.Reason
import content.skill.prayer.getActivePrayerVarKey
import world.gregs.voidps.engine.data.definition.PrayerDefinitions
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasMax
import world.gregs.voidps.engine.get

data class BotPray(val id: String) : BotAction {
    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState {
        val player = bot.player
        val key = player.getActivePrayerVarKey()
        if (player.containsVarbit(key, id)) {
            return BehaviourState.Success
        }
        val def = get<PrayerDefinitions>().getOrNull(id)
            ?: return BehaviourState.Failed(Reason.Invalid("Unknown prayer '$id'."))
        if (!player.hasMax(Skill.Prayer, def.level)) {
            return BehaviourState.Failed(Reason.Invalid("Insufficient prayer level for '$id': need ${def.level}."))
        }
        if (!player.has(Skill.Prayer, 1)) {
            return BehaviourState.Failed(Reason.Invalid("No prayer points to activate '$id'."))
        }
        player.addVarbit(key, id)
        return BehaviourState.Success
    }
}

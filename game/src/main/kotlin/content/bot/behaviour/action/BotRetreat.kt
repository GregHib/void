package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import content.bot.behaviour.Reason
import content.bot.behaviour.condition.Condition
import content.entity.combat.dead
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.player.skill.Skill

data class BotRetreat(
    val safeArea: String,
    val regroupHpPercent: Int,
    val condition: Condition? = null,
) : BotAction {

    override fun start(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState {
        if (condition != null && !condition.check(bot.player)) {
            return BehaviourState.Success
        }
        val def = Areas.getOrNull(safeArea)
            ?: return BehaviourState.Failed(Reason.Invalid("No areas found with id '$safeArea'."))
        if (bot.tile in def.area) {
            return if (regrouped(bot)) BehaviourState.Success else BehaviourState.Running
        }
        if (bot.mode !== EmptyMode) {
            bot.player.mode = EmptyMode
        }
        return BehaviourState.Running
    }

    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState {
        if (bot.player.dead) {
            return BehaviourState.Failed(Reason.Cancelled)
        }
        val def = Areas.getOrNull(safeArea)
            ?: return BehaviourState.Failed(Reason.Invalid("No areas found with id '$safeArea'."))
        if (bot.tile in def.area) {
            return if (regrouped(bot)) BehaviourState.Success else BehaviourState.Running
        }
        if (bot.mode !== EmptyMode) {
            return BehaviourState.Running
        }
        val list = mutableListOf<Int>()
        val success = world.find(bot.player, list, safeArea)
        return BotGoTo.queueRoute(success, list, world, bot, safeArea)
    }

    private fun regrouped(bot: Bot): Boolean {
        val max = bot.levels.getMax(Skill.Constitution)
        if (max <= 0) return true
        val hp = bot.levels.get(Skill.Constitution)
        return hp * 100 >= max * regroupHpPercent
    }
}

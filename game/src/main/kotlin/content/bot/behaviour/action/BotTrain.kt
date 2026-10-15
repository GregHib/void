package content.bot.behaviour.action

import content.bot.Bot
import content.bot.BotManager
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import content.bot.behaviour.Reason
import content.bot.behaviour.activity.BotActivity
import content.bot.behaviour.condition.Condition

/** Schedule through the manager so normal area binding and slot lifecycle apply. */
data class BotTrain(val manager: BotManager, val activity: BotActivity, val goal: Condition) : BotAction {
    override fun start(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState = if (manager.assign(bot, activity, nested = true, completionCondition = goal)) BehaviourState.Running else BehaviourState.Failed(Reason.NoTarget)

    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame) = BehaviourState.Success
}

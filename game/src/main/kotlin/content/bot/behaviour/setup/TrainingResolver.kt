package content.bot.behaviour.setup

import content.bot.BotManager
import content.bot.behaviour.action.BotTrain
import content.bot.behaviour.activity.BotActivity
import content.bot.behaviour.condition.BotSkillLevel

/** The child returns on normal completion or when the caller's base-level goal is met. */
object TrainingResolver {
    fun resolver(manager: BotManager, activity: BotActivity, goal: BotSkillLevel) = Resolver(
        id = "train_skill",
        weight = 1,
        actions = listOf(BotTrain(manager, activity, goal.copy(max = null))),
    )
}

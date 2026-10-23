package content.bot.behaviour.setup

import content.bot.BotManager
import content.bot.behaviour.action.BotAction
import content.bot.behaviour.action.BotCastSpell
import content.bot.behaviour.action.BotFightPlayer
import content.bot.behaviour.action.BotFiremaking
import content.bot.behaviour.action.BotFletchLogs
import content.bot.behaviour.action.BotInteractNpc
import content.bot.behaviour.action.BotInteractObject
import content.bot.behaviour.action.BotRestart
import content.bot.behaviour.action.BotTrain
import content.bot.behaviour.activity.BotActivity
import content.bot.behaviour.condition.BotInventorySetup

/** One finite batch; the parent's pending setup checks the base-level goal again on return. */
object TrainingResolver {
    fun resolver(manager: BotManager, activity: BotActivity) = Resolver(
        id = "train_skill",
        weight = 1,
        actions = listOf(BotTrain(manager, activity)),
    )

    // Admit existing resource-bounded batches, including inventory-bounded restart loops.
    // Arena and other open-ended loops cannot hold a training goal forever.
    fun finite(activity: BotActivity): Boolean {
        if (activity.actions.any {
                it is BotFightPlayer || it is BotCastSpell || (it is BotRestart && !bounded(it))
            }
        ) {
            return false
        }
        val last = activity.actions.lastOrNull()
        return bounded(last) || last is BotFiremaking || last is BotFletchLogs
    }

    private fun bounded(action: BotAction?): Boolean = when (action) {
        is BotInteractObject -> action.success is BotInventorySetup
        is BotInteractNpc -> action.success is BotInventorySetup
        is BotRestart -> action.success is BotInventorySetup
        else -> false
    }
}

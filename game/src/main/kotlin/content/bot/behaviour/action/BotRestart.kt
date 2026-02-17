package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import content.bot.behaviour.Condition
import world.gregs.voidps.engine.entity.character.mode.EmptyMode

/**
 * Restarts the current action when [check] doesn't hold true (or bot has no mode) and success state isn't matched.
 */
data class BotRestart(
    val wait: List<Condition>,
    val success: Condition,
) : BotAction {
    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState {
        if (success.check(bot.player)) {
            return BehaviourState.Success
        }
        if (wait.isEmpty() && bot.mode !is EmptyMode) {
            return BehaviourState.Running
        } else if (wait.any { it.check(bot.player) }) {
            return BehaviourState.Running
        }
        frame.index = 0
        return BehaviourState.Running
    }
}

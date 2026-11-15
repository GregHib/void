package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import content.bot.behaviour.activity.BotActivity

/**
 * The last step of an owned bot's one-off order (go somewhere, attack something, pick something up): the bot
 * goes back to the order it had before, kept in `owned_resume` (following, holding, standing by).
 */
class BotOwnedResume : BotAction {
    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState {
        val resume = bot.player.get<BotActivity>("owned_resume")
        if (resume != null) {
            bot.player.clear("owned_resume")
            bot.pin(resume)
        }
        return BehaviourState.Success
    }
}

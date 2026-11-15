package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld

/**
 * Gets an owned bot to a bank: nothing to do when a booth or chest is already within reach, else the
 * navigation graph's nearest bank. One instance per errand, as the inner go-to keeps route state.
 */
class BotOwnedBankTrip : BotAction {
    private val trip = BotGoToNearest("bank")

    override fun start(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState {
        if (BotOpenBank().nearby(bot.player) != null) {
            return BehaviourState.Success
        }
        return trip.start(bot, world, frame)
    }

    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState? {
        if (BotOpenBank().nearby(bot.player) != null) {
            return BehaviourState.Success
        }
        return trip.update(bot, world, frame)
    }
}

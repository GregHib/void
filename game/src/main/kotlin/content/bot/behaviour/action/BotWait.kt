package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld

data class BotWait(val ticks: Int, val state: BehaviourState = BehaviourState.Success) : BotAction {
    override fun start(bot: Bot, world: BotWorld, frame: BehaviourFrame) = BehaviourState.Wait(ticks, state)
}

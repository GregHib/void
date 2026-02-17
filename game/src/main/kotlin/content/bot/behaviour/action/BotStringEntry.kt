package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import world.gregs.voidps.network.client.instruction.EnterString

data class BotStringEntry(val value: String) : BotAction {
    override fun start(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState {
        bot.player.instructions.trySend(EnterString(value))
        return BehaviourState.Wait(1, BehaviourState.Success)
    }
}

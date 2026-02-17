package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import world.gregs.voidps.network.client.instruction.EnterInt

data class BotIntEntry(val value: Int) : BotAction {
    override fun start(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState {
        world.execute(bot.player, EnterInt(value))
        return BehaviourState.Wait(1, BehaviourState.Success)
    }
}

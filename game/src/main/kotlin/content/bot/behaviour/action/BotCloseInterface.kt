package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import content.bot.behaviour.Reason
import world.gregs.voidps.engine.client.ui.menu
import world.gregs.voidps.network.client.instruction.InterfaceClosedInstruction

object BotCloseInterface : BotAction {
    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState {
        if (bot.player.menu == null) {
            return BehaviourState.Success
        }
        if (world.execute(bot.player, InterfaceClosedInstruction)) {
            return BehaviourState.Success
        }
        return BehaviourState.Failed(Reason.NoTarget)
    }
}

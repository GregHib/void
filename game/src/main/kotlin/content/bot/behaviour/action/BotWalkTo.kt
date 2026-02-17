package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import content.bot.behaviour.Reason
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.network.client.instruction.Walk

data class BotWalkTo(val x: Int, val y: Int, val radius: Int = 4) : BotAction {
    override fun start(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState {
        world.execute(bot.player, Walk(x, y))
        return BehaviourState.Running
    }

    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame) = when {
        bot.tile.within(x, y, bot.tile.level, radius) -> BehaviourState.Success
        bot.mode is EmptyMode && GameLoop.tick - bot.steps.last > 10 -> BehaviourState.Failed(Reason.Stuck)
        else -> BehaviourState.Running
    }
}

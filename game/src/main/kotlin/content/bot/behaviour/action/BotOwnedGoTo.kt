package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import content.bot.behaviour.Reason
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.network.client.instruction.Walk

/**
 * An owned bot walks to the tile its owner pointed at (no jitter, unlike [BotWalkTo]); done once there or
 * beside it (the tile may be taken by whoever the owner pointed past), failed when it stops short of it.
 */
data class BotOwnedGoTo(val x: Int, val y: Int) : BotAction {
    override fun start(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState {
        if (bot.tile.within(x, y, bot.tile.level, 1)) {
            return BehaviourState.Success
        }
        world.execute(bot.player, Walk(x, y))
        return BehaviourState.Running
    }

    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame) = when {
        bot.tile.x == x && bot.tile.y == y -> BehaviourState.Success
        // stopped beside it: the tile is taken by whoever the owner pointed past
        bot.mode is EmptyMode && bot.tile.within(x, y, bot.tile.level, 1) -> BehaviourState.Success
        bot.mode is EmptyMode && GameLoop.tick - bot.steps.last > 10 -> BehaviourState.Failed(Reason.Stuck)
        else -> BehaviourState.Running
    }
}

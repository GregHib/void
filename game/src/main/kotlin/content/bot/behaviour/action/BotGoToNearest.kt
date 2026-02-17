package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import content.bot.behaviour.Reason
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.mode.EmptyMode

data class BotGoToNearest(val tag: String) : BotAction {
    override fun start(bot: Bot, world: BotWorld, frame: BehaviourFrame) = inArea(bot) ?: BehaviourState.Running

    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState {
        if (bot.mode != EmptyMode) {
            return BehaviourState.Running
        }
        val state = inArea(bot)
        if (state != null) {
            return state
        }
        val list = mutableListOf<Int>()
        val success = world.findNearest(bot.player, list, tag)
        return BotGoTo.queueRoute(success, list, world, bot, tag)
    }

    private fun inArea(bot: Bot): BehaviourState? {
        val set = Areas.tagged(tag)
        if (set.isEmpty()) {
            return BehaviourState.Failed(Reason.Invalid("No areas tagged with tag '$tag'."))
        }
        if (set.any { bot.tile in it.area }) {
            return BehaviourState.Success
        }
        return null
    }
}

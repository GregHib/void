package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import content.bot.behaviour.condition.BotInterfaceOpen
import content.bot.behaviour.setup.BankAccess
import world.gregs.voidps.engine.entity.character.mode.EmptyMode

/** Open a nearby supported booth or chest through its actual game option. */
class BotOpenBank : BotAction {
    override fun start(bot: Bot, world: BotWorld, frame: BehaviourFrame) =
        if (BotInterfaceOpen("bank").check(bot.player)) BehaviourState.Success else BehaviourState.Running

    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState? {
        if (BotInterfaceOpen("bank").check(bot.player)) return BehaviourState.Success
        if (bot.mode != EmptyMode) return BehaviourState.Running
        val (obj, option) = BankAccess.nearby(bot.player) ?: return BehaviourState.Running
        return BotInteractObject(option, obj.id, success = BotInterfaceOpen("bank"), radius = 0, x = obj.x, y = obj.y).update(bot, world, frame)
    }
}

package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import content.bot.behaviour.Reason
import content.bot.behaviour.navigation.NavigationShortcut
import content.bot.behaviour.setup.Resolver
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

data class BotGoTo(val target: String) : BotAction {
    override fun start(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState {
        val def = Areas.getOrNull(target) ?: return BehaviourState.Failed(Reason.Invalid("No areas found with id '$target'."))
        if (bot.tile in def.area) {
            return BehaviourState.Success
        }
        return BehaviourState.Running
    }

    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState {
        if (bot.mode != EmptyMode) {
            return BehaviourState.Running
        }
        val def = Areas.getOrNull(target) ?: return BehaviourState.Failed(Reason.Invalid("No areas found with id '$target'."))
        if (bot.tile in def.area) {
            return BehaviourState.Success
        }

        val list = mutableListOf<Int>()
        val success = world.find(bot.player, list, target)
        return queueRoute(success, list, world, bot, target)
    }

    companion object {
        internal fun queueRoute(success: Boolean, list: MutableList<Int>, world: BotWorld, bot: Bot, target: String): BehaviourState {
            if (!success) {
                return BehaviourState.Failed(Reason.NoRoute)
            }
            val actions = mutableListOf<BotAction>()
            var nav: NavigationShortcut? = null
            for (edge in list) {
                val shortcut = world.shortcut(edge)
                if (shortcut != null) {
                    nav = shortcut
                } else {
                    actions.addAll(world.actions(edge) ?: continue)
                }
            }
            if (actions.isNotEmpty()) {
                bot.queue(BehaviourFrame(Resolver("go_to_$target", 0, TimeUnit.SECONDS.toTicks(60), actions = actions)))
            }
            if (nav != null) {
                bot.queue(BehaviourFrame(nav))
            }
            if (bot.frames.isEmpty()) {
                return BehaviourState.Failed(Reason.NoRoute)
            }
            return BehaviourState.Running
        }
    }
}

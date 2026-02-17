package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import world.gregs.voidps.engine.get
import world.gregs.voidps.network.client.instruction.*
import kotlin.collections.iterator

sealed interface BotAction {
    fun start(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState = BehaviourState.Running
    fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState? = null
}

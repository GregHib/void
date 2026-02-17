package content.bot.behaviour

import content.bot.Bot
import content.bot.behaviour.action.BotAction

/**
 * Tracks an ongoing [behaviour], it's [state] which action [index] is in progress
 * [timeout] and any actions which are [blocked] from being retried.
 */
data class BehaviourFrame(
    val behaviour: Behaviour,
    var state: BehaviourState = BehaviourState.Pending,
    var index: Int = 0,
    var blocked: MutableSet<String> = mutableSetOf(),
    var timeout: Int = 0,
) {

    fun action(): BotAction = behaviour.actions[index]

    fun completed() = index >= behaviour.actions.size

    fun start(bot: Bot, world: BotWorld) {
        val action = action()
        state = action.start(bot, world, this)
    }

    fun update(bot: Bot, world: BotWorld) {
        if (++timeout > behaviour.timeout) {
            fail(Reason.Timeout)
            return
        }
        val action = action()
        state = action.update(bot, world, this) ?: return
    }

    fun next(): Boolean {
        if (index >= behaviour.actions.lastIndex) {
            return false
        }
        index++
        state = BehaviourState.Running
        return true
    }

    fun fail(reason: Reason) {
        state = BehaviourState.Failed(reason)
    }

    fun success() {
        if (state is BehaviourState.Failed) {
            return
        }
        state = BehaviourState.Success
    }
}

package content.bot.action

import content.bot.Bot

data class BehaviourFrame(
    val behaviour: Behaviour,
    var state: BehaviourState = BehaviourState.Pending,
    var index: Int = 0,
    var retries: Int = 0,
    var blocked: MutableSet<String> = mutableSetOf(),
) {

    fun action(): BotAction = behaviour.actions[index]

    fun completed() = index >= behaviour.actions.size

    fun start(bot: Bot) {
        val action = action()
        state = action.start(bot)
    }

    fun update(bot: Bot) {
        val action = action()
        state = action.update(bot) ?: return
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
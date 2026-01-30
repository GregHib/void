package content.bot.action

import content.bot.Bot

data class BehaviourFrame(
    val behaviour: Behaviour,
    var state: BehaviourState = BehaviourState.Pending,
    var index: Int = 0,
    var retries: Int = 0,
    var blocked: MutableSet<String> = mutableSetOf(),
) {

    fun action(): BotAction = behaviour.plan[index]

    fun completed() = index >= behaviour.plan.size

    fun start(bot: Bot) {
        val action = action()
        state = action.start(bot)
    }

    fun update(bot: Bot) {
        val action = action()
        state = action.update(bot)
    }

    fun next(): Boolean {
        index++
        state = BehaviourState.Pending
        return index < behaviour.plan.size
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
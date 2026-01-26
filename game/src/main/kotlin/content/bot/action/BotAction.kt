package content.bot.action

import content.bot.Bot

sealed interface BotAction {
    open fun start(bot: Bot) {
        // TODO here's where code goes, either handle this way or put in an event handler for each type
    }

    sealed class RetryableAction : BotAction {
        abstract val retryTicks: Int
        abstract val retryMax: Int
    }

    data class GoTo(val target: String) : BotAction
    data class Clone(val id: String) : BotAction

    data class Wait(val ticks: Int) : BotAction

    data class InteractNpc(
        val option: String,
        val id: String,
        override val retryTicks: Int = 0,
        override val retryMax: Int = 0,
        val radius: Int = 10,
    ) : RetryableAction()

    data class InteractObject(
        val option: String,
        val id: String,
        override val retryTicks: Int = 0,
        override val retryMax: Int = 0,
        val radius: Int = 10,
    ) : RetryableAction()

    data class InterfaceOption(val option: String, val id: String) : BotAction
    data class WaitFullInventory(val timeout: Int) : BotAction
}
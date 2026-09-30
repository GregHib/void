package content.bot.chat.api

/**
 * Something the bot asked and is waiting on a yes/no for, e.g. "Do you need help?"
 */
class Expectation(
    val topic: String,
    val expires: Long,
    val yes: ChatContext.() -> Unit,
    val no: ChatContext.() -> Unit,
)
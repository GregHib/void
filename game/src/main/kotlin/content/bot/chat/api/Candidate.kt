package content.bot.chat.api

/**
 * A quick chat phrase the bot could send. [then] runs only if this candidate is picked.
 */
class Candidate(
    val phrase: String?,
    val args: List<String>,
    val weight: Float,
    val intent: String,
    val expectation: Expectation?,
    val then: (() -> Unit)?,
)
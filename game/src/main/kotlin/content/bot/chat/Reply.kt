package content.bot.chat

/**
 * A quick chat reply ready to send, [then] is the chosen candidate's side effect
 */
data class Reply(val phrase: String, val args: List<String>, val id: Int, val data: ByteArray, val then: (() -> Unit)?)
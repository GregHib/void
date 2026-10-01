package content.bot.chat

/**
 * A reply ready to send, either a quick chat [phrase] with its [id] and encoded [data] or [typed] text, [then] is the chosen candidate's side effect
 */
data class BotChatReply(val phrase: String, val args: List<String>, val id: Int, val data: ByteArray, val then: (() -> Unit)?) {
    val typed: Boolean
        get() = id == TYPED

    companion object {
        const val TYPED = -1

        fun typed(text: String, then: (() -> Unit)?) = BotChatReply(text, emptyList(), TYPED, ByteArray(0), then)
    }
}

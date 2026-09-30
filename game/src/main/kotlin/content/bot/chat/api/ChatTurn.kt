package content.bot.chat.api

import content.bot.chat.tag.ChatEntity

data class ChatTurn(val fromBot: Boolean, val intent: String, val entities: List<ChatEntity>, val text: String, val time: Long)

package content.bot.chat.api

import content.bot.chat.tag.ChatEntity
import content.bot.chat.tag.ChatEntityType

/**
 * What a player said, reduced to an intent and the entities they mentioned
 */
data class Utterance(
    val text: String,
    val tokens: List<String>,
    val intent: String,
    val confidence: Float,
    val entities: List<ChatEntity>,
) {
    fun first(type: ChatEntityType): ChatEntity? = entities.firstOrNull { it.type == type }

    fun mentions(word: String) = tokens.contains(word)
}

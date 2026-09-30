package content.bot.chat.tag

/**
 * A known game entity, [key] is a string id e.g. "rune_scimitar", "mining", "varrock"
 */
data class ChatEntity(val type: ChatEntityType, val key: String)
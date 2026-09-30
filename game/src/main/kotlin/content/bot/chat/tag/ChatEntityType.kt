package content.bot.chat.tag

/**
 * Entity types in priority order, when two aliases of the same length match the earlier type wins.
 * [fuzzy] types allow a one letter typo, bulk types (items/npcs) only match exactly to avoid false positives.
 */
enum class ChatEntityType(val placeholder: String, val fuzzy: Boolean) {
    Skill("{skill}", true),
    Location("{location}", true),
    Minigame("{minigame}", true),
    Quest("{quest}", true),
    Item("{item}", false),
    Npc("{npc}", false),
    Number("{number}", false),
}
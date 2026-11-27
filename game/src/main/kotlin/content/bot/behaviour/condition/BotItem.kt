package content.bot.behaviour.condition

data class BotItem(
    val ids: Set<String>,
    val min: Int? = null,
    val max: Int? = null,
    val usable: Boolean = false,
    val equippable: Boolean = false,
) {
    /**
     * The id to give when handing this item out: the first unnoted, undegraded option, otherwise the first which isn't "empty"
     */
    val primaryId: String?
        get() = ids.firstOrNull { it != "empty" && !it.endsWith("_noted") && !it.endsWith("_broken") } ?: ids.firstOrNull { it != "empty" }
}

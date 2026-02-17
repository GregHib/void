package content.bot.behaviour.condition

data class BotItem(
    val ids: Set<String>,
    val min: Int? = null,
    val max: Int? = null,
    val usable: Boolean = false,
    val equippable: Boolean = false,
)

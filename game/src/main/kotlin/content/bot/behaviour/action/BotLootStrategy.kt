package content.bot.behaviour.action

import world.gregs.voidps.engine.entity.item.floor.FloorItem

/**
 * Controls which floor items a bot is willing to pick up after a kill,
 * and the order in which competing piles are evaluated.
 */
enum class BotLootStrategy {
    /** Loot any owned item above the value threshold, in spiral order. */
    DEFAULT,

    /** Only loot consumables (food, potions, prayer restores). */
    SURVIVAL,

    /** Loot any owned item above the value threshold, picking the highest total value first. */
    WEALTH;

    /** Whether [item] is eligible to be looted under this strategy. */
    fun accepts(item: FloorItem): Boolean = when (this) {
        SURVIVAL -> isConsumable(item)
        DEFAULT, WEALTH -> true
    }

    /** Whether candidates should be gathered and ranked instead of taking the first match. */
    fun ranks(): Boolean = this == WEALTH

    /** Total coin value to compare candidates by; higher is better. */
    fun score(item: FloorItem): Long = item.value

    companion object {
        private val SURVIVAL_CATEGORIES = setOf("edible", "potion", "prayer_consumable")

        fun of(name: String?): BotLootStrategy = when (name?.lowercase()) {
            null, "default" -> DEFAULT
            "survival" -> SURVIVAL
            "wealth" -> WEALTH
            else -> error("Unknown loot strategy '$name'. Expected 'survival', 'wealth' or 'default'.")
        }

        private fun isConsumable(item: FloorItem): Boolean {
            val def = item.def
            if (def.options.contains("Eat") || def.options.contains("Drink")) {
                return true
            }
            val categories: Set<String> = def.getOrNull("categories") ?: return false
            return SURVIVAL_CATEGORIES.any { categories.contains(it) }
        }
    }
}

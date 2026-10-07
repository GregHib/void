package content.bot

import kotlin.random.Random

/** Chooses a spawn pool, falling back when no name in that pool can be reserved. */
internal object BotNamePicker {
    fun pick(savedNames: List<String>, savedPercent: Int, random: Random, reserve: (String) -> Boolean, fresh: () -> String?): String? {
        fun saved(): String? = savedNames.shuffled(random).firstOrNull(reserve)
        return if (random.nextInt(100) < savedPercent) saved() ?: fresh() else fresh() ?: saved()
    }
}

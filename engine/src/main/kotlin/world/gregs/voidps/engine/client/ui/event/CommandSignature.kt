package world.gregs.voidps.engine.client.ui.event

import world.gregs.voidps.engine.entity.character.player.Player

/**
 * Command argument signature
 * For commands that can take multiple different types of arguments
 */
data class CommandSignature(
    val args: List<CommandArgument> = emptyList(),
    val description: String = "",
    val handler: suspend (Player, List<String>) -> Unit,
) {

    /**
     * Score how many passed of the arguments are valid
     */
    fun score(input: List<String>): Int? {
        val requiredCount = args.count { !it.optional }
        val maxCount = args.size

        if (input.size < requiredCount) {
            return null
        }
        if (input.size > maxCount) {
            return null
        }

        var score = 0
        for (i in input.indices) {
            val arg = args.getOrNull(i) ?: return null
            if (!arg.canParse(input[i])) {
                return null
            }
            score++
        }
        return score
    }

    fun usage(): String {
        return args.joinToString(" ")
    }
}
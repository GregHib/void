package world.gregs.voidps.engine.client.command

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
        if (input.size < requiredCount) {
            return null
        }
        return valid(input)
    }

    /**
     * Score how many passed of the arguments are valid
     */
    fun valid(input: List<String>): Int? {
        if (input.size > args.size) {
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
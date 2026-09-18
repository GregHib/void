package world.gregs.voidps.engine.client.command

/**
 * Data about a console command
 * Unlike [Command] there is no sender; console commands are run by the operator of the server
 * process and have no rights to check.
 */
data class ConsoleCommand(
    val name: String,
    val args: List<CommandArgument> = emptyList(),
    val description: String = "",
    /**
     * Whether the last argument takes the rest of the line, so it doesn't have to be quoted.
     */
    val rest: Boolean = false,
    val handler: (List<String>) -> List<String>,
) {
    fun usage(): String {
        if (args.isEmpty()) {
            return name
        }
        val trailing = if (rest) "..." else ""
        return "$name ${args.joinToString(" ")}$trailing"
    }
}

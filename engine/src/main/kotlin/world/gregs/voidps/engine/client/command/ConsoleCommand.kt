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
    val handler: (List<String>) -> List<String>,
) {
    fun usage(): String {
        if (args.isEmpty()) {
            return name
        }
        return "$name ${args.joinToString(" ")}"
    }
}

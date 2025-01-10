package world.gregs.voidps.engine.client.ui.event

import world.gregs.voidps.engine.client.ui.chat.Colours
import world.gregs.voidps.engine.client.ui.chat.toTag
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

data class Command(
    override val character: Character,
    val prefix: String,
    val content: String
) : Interaction() {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }

    override val size = 3

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "command"
        1 -> prefix
        2 -> character["rights", "none"]
        else -> null
    }

    companion object {
        var count = 0
        val adminCommands = mutableListOf(
            "Commands list with descriptions and usage instructions in the format:",
            "${Colours.BLUE.toTag()}command_name (required-variable) [optional-variable]</col>",
            "command description",
            ""
        )
        val modCommands = mutableListOf<String>()
    }
}

fun adminCommand(command: String, description: String = "", aliases: List<String> = emptyList(), block: suspend Command.() -> Unit) {
    if (description.isNotBlank()) {
        Command.adminCommands.add("${Colours.BLUE.toTag()}${command}</col>")
    }
    val index = command.indexOfFirst { it == '(' || it == '[' }
    val commandName = (if (index != -1) command.substring(0, index) else command).trim()
    val handler: suspend Command.(Player) -> Unit = {
        block.invoke(this)
    }
    Events.handle("command", commandName, "admin", handler = handler)
    for (alias in aliases) {
        if (description.isNotBlank()) {
            Command.adminCommands.add("${Colours.BLUE.toTag()}${command.replace(commandName, alias)}</col>")
        }
        Events.handle("command", alias, "admin", handler = handler)
    }

    if (description.isNotBlank()) {
        Command.adminCommands.add(description)
        Command.adminCommands.add("")
    }
}

fun modCommand(command: String, description: String = "", aliases: List<String> = emptyList(), block: suspend Command.() -> Unit) {
    if (description.isNotBlank()) {
        Command.modCommands.add("${Colours.BLUE.toTag()}${command}</col>")
        Command.adminCommands.add("${Colours.BLUE.toTag()}${command}</col>")
    }
    val index = command.indexOfFirst { it == '(' || it == '[' }
    val commandName = (if (index != -1) command.substring(0, index) else command).trim()
    val handler: suspend Command.(Player) -> Unit = {
        block.invoke(this)
    }
    Events.handle("command", commandName, "mod", handler = handler)
    Events.handle("command", commandName, "admin", handler = handler)
    for (alias in aliases) {
        if (description.isNotBlank()) {
            Command.modCommands.add("${Colours.BLUE.toTag()}${command.replace(commandName, alias)}</col>")
            Command.adminCommands.add("${Colours.BLUE.toTag()}${command.replace(commandName, alias)}</col>")
        }
        Events.handle("command", alias, "mod", handler = handler)
        Events.handle("command", alias, "admin", handler = handler)
    }
    if (description.isNotBlank()) {
        Command.modCommands.add(description)
        Command.adminCommands.add(description)
        Command.modCommands.add("")
        Command.adminCommands.add("")
    }
}
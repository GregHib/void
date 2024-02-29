package world.gregs.voidps.engine.client.ui.event

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

    override fun size() = 3

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "command"
        1 -> prefix
        2 -> character["rights", "none"]
        else -> ""
    }
}

fun adminCommand(vararg commands: String, block: suspend Command.() -> Unit) {
    val handler: suspend Command.(Player) -> Unit = {
        block.invoke(this)
    }
    for (command in commands) {
        Events.handle("command", command, "admin", block = handler)
    }
}

fun modCommand(vararg commands: String, block: suspend Command.() -> Unit) {
    val handler: suspend Command.(Player) -> Unit = {
        block.invoke(this)
    }
    for (command in commands) {
        Events.handle("command", command, "mod", block = handler)
        Events.handle("command", command, "admin", block = handler)
    }
}
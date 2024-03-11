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

    override val size = 3

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "command"
        1 -> prefix
        2 -> character["rights", "none"]
        else -> null
    }
}

fun adminCommand(vararg commands: String, block: suspend Command.() -> Unit) {
    val handler: suspend Command.(Player) -> Unit = {
        block.invoke(this)
    }
    for (command in commands) {
        Events.handle("command", command, "admin", handler = handler)
    }
}

fun modCommand(vararg commands: String, block: suspend Command.() -> Unit) {
    val handler: suspend Command.(Player) -> Unit = {
        block.invoke(this)
    }
    for (command in commands) {
        Events.handle("command", command, "mod", handler = handler)
        Events.handle("command", command, "admin", handler = handler)
    }
}
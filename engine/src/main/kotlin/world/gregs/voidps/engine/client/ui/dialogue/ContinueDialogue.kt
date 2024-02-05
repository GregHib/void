package world.gregs.voidps.engine.client.ui.dialogue

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on

data class ContinueDialogue(
    val id: String,
    val component: String,
    val option: Int
) : Event

fun continueDialogue(filter: ContinueDialogue.(Player) -> Boolean = { true }, priority: Priority = Priority.MEDIUM, block: suspend ContinueDialogue.(Player) -> Unit) {
    on<ContinueDialogue>(filter, priority, block)
}
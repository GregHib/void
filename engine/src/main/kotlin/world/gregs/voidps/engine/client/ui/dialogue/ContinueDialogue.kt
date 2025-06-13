package world.gregs.voidps.engine.client.ui.dialogue

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

data class ContinueDialogue(
    val id: String,
    val component: String,
    val option: Int,
) : Event {

    override val size = 4

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "continue_dialogue"
        1 -> id
        2 -> component
        3 -> option
        else -> null
    }
}

fun continueDialogue(id: String = "*", component: String = "*", option: String = "*", handler: suspend ContinueDialogue.(Player) -> Unit) {
    Events.handle("continue_dialogue", id, component, option, handler = handler)
}

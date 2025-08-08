package world.gregs.voidps.engine.client.ui.dialogue

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

data class ContinueItemDialogue(
    val item: String,
) : Event {

    override val size = 2

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "continue_item_dialogue"
        1 -> item
        else -> null
    }
}

fun continueItemDialogue(id: String = "*", handler: suspend ContinueItemDialogue.(Player) -> Unit) {
    Events.handle("continue_item_dialogue", id, handler = handler)
}

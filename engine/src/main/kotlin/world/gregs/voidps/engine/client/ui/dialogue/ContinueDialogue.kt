package world.gregs.voidps.engine.client.ui.dialogue

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.wildcardEquals

data class ContinueDialogue(
    val id: String,
    val component: String,
    val option: Int
) : Event

fun continueDialogue(id: String = "*", component: String = "*", block: suspend ContinueDialogue.(Player) -> Unit) {
    on<ContinueDialogue>({ wildcardEquals(id, this.id) && wildcardEquals(component, this.component) }, block = block)
}
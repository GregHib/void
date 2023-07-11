package world.gregs.voidps.world.interact.dialogue

import world.gregs.voidps.engine.client.ui.Interfaces
import world.gregs.voidps.engine.data.definition.AnimationDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.suspend.dialogue.ContinueSuspension

fun Interfaces.sendLines(id: String, lines: List<String>) {
    for ((index, line) in lines.withIndex()) {
        sendText(id, "line${index + 1}", line)
    }
}

fun Interfaces.sendChat(
    id: String,
    component: String,
    expression: String,
    title: String,
    lines: List<String>
) {
    val animationDefs: AnimationDefinitions = get()
    val definition = animationDefs.getOrNull("expression_$expression${lines.size}") ?: animationDefs.get("expression_$expression")
    sendAnimation(id, component, definition.id)
    sendText(id, "title", title)
    sendLines(id, lines)
}

fun Player.continueDialogue() {
    val suspension = dialogueSuspension as? ContinueSuspension ?: return
    this.dialogueSuspension = null
    suspension.resume()
}
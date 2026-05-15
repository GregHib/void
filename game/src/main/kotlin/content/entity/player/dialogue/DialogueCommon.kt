package content.entity.player.dialogue

import world.gregs.voidps.engine.client.ui.Interfaces
import world.gregs.voidps.engine.data.definition.AnimationDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.suspend.Suspension

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
    lines: List<String>,
) {
    val definition = AnimationDefinitions.getOrNull("expression_$expression${lines.size}") ?: AnimationDefinitions.get("expression_$expression")
    sendAnimation(id, component, definition.id)
    sendText(id, "title", title)
    sendLines(id, lines)
}

fun Player.continueDialogue() {
    (suspension as? Suspension.Continue)?.resume()
}

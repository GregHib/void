package world.gregs.voidps.world.interact.dialogue

import world.gregs.voidps.engine.client.ui.Interfaces
import world.gregs.voidps.engine.client.ui.sendAnimation
import world.gregs.voidps.engine.client.ui.sendText
import world.gregs.voidps.engine.entity.definition.AnimationDefinitions
import world.gregs.voidps.engine.utility.get

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
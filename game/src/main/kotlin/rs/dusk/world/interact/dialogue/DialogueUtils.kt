package rs.dusk.world.interact.dialogue

import rs.dusk.engine.client.ui.Interfaces

fun Interfaces.sendLines(name: String, lines: List<String>) {
    for ((index, line) in lines.withIndex()) {
        sendText(name, "line${index + 1}", line)
    }
}
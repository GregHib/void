package rs.dusk.world.interact.dialogue

import rs.dusk.engine.client.ui.dialogue.Dialogues
import rs.dusk.engine.client.ui.open
import rs.dusk.engine.client.variable.setVar

private const val LEVEL_UP_INTERFACE_NAME = "level_up_dialog"

suspend fun Dialogues.levelUp(text: String, skill: Int) {
    val lines = text.trimIndent().lines()
    if (player.open(LEVEL_UP_INTERFACE_NAME)) {
        player.interfaces.sendLines(LEVEL_UP_INTERFACE_NAME, lines)
        player.setVar("level_up_icon", skill)
        return await("level")
    }
}
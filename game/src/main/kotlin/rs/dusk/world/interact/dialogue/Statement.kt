package rs.dusk.world.interact.dialogue

import com.github.michaelbull.logging.InlineLogger
import rs.dusk.engine.client.ui.dialogue.DialogueContext
import rs.dusk.engine.client.ui.open
import rs.dusk.engine.entity.character.player.Player

private const val MAXIMUM_STATEMENT_SIZE = 5
private val logger = InlineLogger()

suspend fun DialogueContext.statement(text: String, clickToContinue: Boolean = true) {
    val lines = text.trimIndent().lines()

    if (lines.size > MAXIMUM_STATEMENT_SIZE) {
        logger.warn { "Maximum statement lines exceeded ${lines.size} for $player" }
        return
    }

    val name = getInterfaceName("message", lines.size, clickToContinue)
    if (player.open(name)) {
        sendLines(player, name, lines)
        await<Unit>("statement")
    }
}

private fun sendLines(player: Player, name: String, lines: List<String>) {
    for ((index, line) in lines.withIndex()) {
        player.interfaces.sendText(name, "line${index + 1}", line)
    }
}

private fun getInterfaceName(name: String, lines: Int, prompt: Boolean): String {
    return "$name${if (!prompt) "_np" else ""}$lines"
}
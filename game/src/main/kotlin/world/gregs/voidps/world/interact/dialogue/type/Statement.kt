package world.gregs.voidps.world.interact.dialogue.type

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.ui.dialogue.DialogueContext
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.world.interact.dialogue.sendLines

private const val MAXIMUM_STATEMENT_SIZE = 5
private val logger = InlineLogger()

suspend fun DialogueContext.statement(text: String, clickToContinue: Boolean = true) {
    val lines = text.trimIndent().lines()

    if (lines.size > MAXIMUM_STATEMENT_SIZE) {
        logger.debug { "Maximum statement lines exceeded ${lines.size} for $player" }
        return
    }

    val id = getInterfaceId(lines.size, clickToContinue)
    if (player.open(id)) {
        player.interfaces.sendLines(id, lines)
        await<Unit>("statement")
    }
}

private fun getInterfaceId(lines: Int, prompt: Boolean): String {
    return "message${if (!prompt) "_np" else ""}$lines"
}
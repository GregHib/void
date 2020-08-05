package rs.dusk.world.interact.dialogue.type

import com.github.michaelbull.logging.InlineLogger
import rs.dusk.engine.client.ui.dialogue.DialogueContext
import rs.dusk.engine.client.ui.open
import rs.dusk.engine.entity.character.player.Player

private val CHOICE_LINE_RANGE = 2..5
private const val APPROXIMATE_WIDE_TITLE_LENGTH = 30
private val logger = InlineLogger()

suspend fun DialogueContext.choice(text: String, title: String? = null, saySelection: Boolean = true): Int {
    val lines = text.trimIndent().lines()

    if (lines.size !in CHOICE_LINE_RANGE) {
        logger.debug { "Invalid choice line count ${lines.size} for $player" }
        return -1
    }

    val multilineTitle = title != null && isMultiline(title)
    val multilineOptions = lines.any { isMultiline(it) }
    val name = getChoiceName(
        multilineTitle,
        multilineOptions,
        lines.size
    )
    if (player.open(name)) {
        if (title != null) {
            val wide = title.length > APPROXIMATE_WIDE_TITLE_LENGTH
            player.interfaces.sendVisibility(name, "wide_swords", wide)
            player.interfaces.sendVisibility(name, "thin_swords", !wide)
            player.interfaces.sendText(name, "title", title)
        }

        sendLines(player, name, lines)
        val choice = await<Int>("choice")
        if (saySelection) {
            val line = lines.getOrNull(choice - 1)
            if (line != null) {
                say(text = line)
            }
        }
        return choice
    }
    return -1
}

private fun isMultiline(string: String): Boolean = string.contains("<br>")

private fun getChoiceName(multilineTitle: Boolean, multilineOptions: Boolean, lines: Int): String {
    return "multi${if (multilineTitle) "_var" else ""}$lines${if (multilineOptions) "_chat" else ""}"
}

private fun sendLines(player: Player, name: String, lines: List<String>) {
    for ((index, line) in lines.withIndex()) {
        player.interfaces.sendText(name, "line${index + 1}", line)
    }
}
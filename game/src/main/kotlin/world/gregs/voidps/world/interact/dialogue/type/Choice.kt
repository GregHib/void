package world.gregs.voidps.world.interact.dialogue.type

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.ui.dialogue.DialogueContext
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.ui.sendText
import world.gregs.voidps.engine.client.ui.sendVisibility
import world.gregs.voidps.engine.entity.character.player.Player

private val CHOICE_LINE_RANGE = 2..5
private const val APPROXIMATE_WIDE_TITLE_LENGTH = 30
private val logger = InlineLogger()

suspend fun DialogueContext.choice(text: String, title: String? = null): Int {
    val lines = text.trimIndent().lines()

    if (lines.size !in CHOICE_LINE_RANGE) {
        logger.debug { "Invalid choice line count ${lines.size} for $player" }
        return -1
    }

    val question = title?.trimIndent()?.replace("\n", "<br>")
    val multilineTitle = question?.contains("<br>") ?: false
    val multilineOptions = lines.any { isMultiline(it) }
    val id = getChoiceId(multilineTitle, multilineOptions, lines.size)
    if (player.open(id)) {
        if (question != null) {
            val longestLine = question.split("<br>").maxByOrNull { it.length }?.length ?: 0
            val wide = longestLine > APPROXIMATE_WIDE_TITLE_LENGTH
            player.interfaces.sendVisibility(id, "wide_swords", wide)
            player.interfaces.sendVisibility(id, "thin_swords", !wide)
            player.interfaces.sendText(id, "title", question)
        }

        sendLines(player, id, lines)
        return await("choice")
    }
    return -1
}

private fun isMultiline(string: String): Boolean = string.contains("<br>")

private fun getChoiceId(multilineTitle: Boolean, multilineOptions: Boolean, lines: Int): String {
    return "multi${if (multilineTitle) "_var" else ""}$lines${if (multilineOptions) "_chat" else ""}"
}

private fun sendLines(player: Player, name: String, lines: List<String>) {
    for ((index, line) in lines.withIndex()) {
        player.interfaces.sendText(name, "line${index + 1}", line)
    }
}
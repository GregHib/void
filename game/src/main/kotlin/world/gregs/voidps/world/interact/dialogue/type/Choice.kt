package world.gregs.voidps.world.interact.dialogue.type

import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.ui.sendText
import world.gregs.voidps.engine.client.ui.sendVisibility
import world.gregs.voidps.engine.entity.character.player.PlayerContext
import world.gregs.voidps.engine.suspend.IntSuspension
import world.gregs.voidps.world.interact.dialogue.sendLines

private val CHOICE_LINE_RANGE = 2..5
private const val APPROXIMATE_WIDE_TITLE_LENGTH = 30

suspend fun PlayerContext.choice(text: String, title: String? = null): Int {
    val lines = text.trimIndent().lines()
    check(lines.size in CHOICE_LINE_RANGE) { "Invalid choice line count ${lines.size} for $player" }
    val question = title?.trimIndent()?.replace("\n", "<br>")
    val multilineTitle = question?.contains("<br>") ?: false
    val multilineOptions = lines.any { isMultiline(it) }
    val id = getChoiceId(multilineTitle, multilineOptions, lines.size)
    check(player.open(id)) { "Unable to open choice dialogue for $player" }
    if (question != null) {
        val longestLine = question.split("<br>").maxByOrNull { it.length }?.length ?: 0
        val wide = longestLine > APPROXIMATE_WIDE_TITLE_LENGTH
        player.interfaces.sendVisibility(id, "wide_swords", wide)
        player.interfaces.sendVisibility(id, "thin_swords", !wide)
        player.interfaces.sendText(id, "title", question)
    }
    player.interfaces.sendLines(id, lines)
    val result = IntSuspension()
    player.close(id)
    return result
}

private fun isMultiline(string: String): Boolean = string.contains("<br>")

private fun getChoiceId(multilineTitle: Boolean, multilineOptions: Boolean, lines: Int): String {
    return "dialogue_multi${if (multilineTitle) "_var" else ""}$lines${if (multilineOptions) "_chat" else ""}"
}

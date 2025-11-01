package content.entity.player.dialogue.type

import content.entity.player.dialogue.sendLines
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.suspend.IntSuspension
import world.gregs.voidps.engine.suspend.SuspendableContext

private val CHOICE_LINE_RANGE = 2..5
private const val APPROXIMATE_WIDE_TITLE_LENGTH = 30

/**
 * Usage:
 *  choice("Pick an option") {
 *      option("one") {
 *          // ...
 *      }
 *      option<Happy>("two") {
 *      }
 *      option("three", ::condition) {
 *      }
 *  }
 */
suspend fun <T : SuspendableContext<Player>> T.choice(title: String? = null, block: suspend ChoiceBuilder<T>.() -> Unit) {
    val builder = ChoiceBuilder<T>()
    block.invoke(builder)
    val lines = builder.build(this)
    if (lines.size == 1) {
        builder.invoke(0, this)
        return
    }
    val choice = choice(lines, title)
    builder.invoke(choice - 1, this)
}

/**
 * Usage:
 *  val choice = choice(listOf("One", "Two", "Three"))
 *  if (choice == 1) {
 *     // ...
 *  }
 */
suspend fun SuspendableContext<Player>.choice(lines: List<String>, title: String? = null): Int {
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
    val result = IntSuspension.get(player)
    player.close(id)
    return result
}

/**
 * Usage:
 *  choice("Pick an option") {
 *      option("one") {
 *          // ...
 *      }
 *      option<Happy>("two") {
 *      }
 *      option("three", ::condition) {
 *      }
 *  }
 */
suspend fun Player.choice(title: String? = null, block: suspend ChoiceOption.() -> Unit) {
    val builder = ChoiceOption()
    block.invoke(builder)
    val lines = builder.build(this)
    if (lines.size == 1) {
        builder.invoke(0, this)
        return
    }
    val choice = choice(lines, title)
    builder.invoke(choice - 1, this)
}

/**
 * Usage:
 *  val choice = choice(listOf("One", "Two", "Three"))
 *  if (choice == 1) {
 *     // ...
 *  }
 */
suspend fun Player.choice(lines: List<String>, title: String? = null): Int {
    check(lines.size in CHOICE_LINE_RANGE) { "Invalid choice line count ${lines.size} for $this" }
    val question = title?.trimIndent()?.replace("\n", "<br>")
    val multilineTitle = question?.contains("<br>") ?: false
    val multilineOptions = lines.any { isMultiline(it) }
    val id = getChoiceId(multilineTitle, multilineOptions, lines.size)
    check(open(id)) { "Unable to open choice dialogue for $this" }
    if (question != null) {
        val longestLine = question.split("<br>").maxByOrNull { it.length }?.length ?: 0
        val wide = longestLine > APPROXIMATE_WIDE_TITLE_LENGTH
        interfaces.sendVisibility(id, "wide_swords", wide)
        interfaces.sendVisibility(id, "thin_swords", !wide)
        interfaces.sendText(id, "title", question)
    }
    interfaces.sendLines(id, lines)
    val result = IntSuspension.get(this)
    close(id)
    return result
}

private fun isMultiline(string: String): Boolean = string.contains("<br>")

private fun getChoiceId(multilineTitle: Boolean, multilineOptions: Boolean, lines: Int): String = "dialogue_multi${if (multilineTitle) "_var" else ""}$lines${if (multilineOptions) "_chat" else ""}"

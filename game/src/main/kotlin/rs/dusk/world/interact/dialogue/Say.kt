package rs.dusk.world.interact.dialogue

import com.github.michaelbull.logging.InlineLogger
import rs.dusk.engine.client.ui.dialogue.DialogueContext
import rs.dusk.engine.client.ui.dialogue.Expression
import rs.dusk.engine.client.ui.open
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.update.visual.player.name

private val logger = InlineLogger()

suspend fun DialogueContext.say(text: String, expression: Expression = Expression.Talking, largeHead: Boolean = false, clickToContinue: Boolean = true, title: String? = null) {
    val lines = text.trimIndent().lines()

    if (lines.size > 4) {
        logger.warn { "Maximum player chat lines exceeded ${lines.size} for $player" }
        return
    }

    val name = getInterfaceName("chat", lines.size, clickToContinue)

    if (player.open(name)) {
        val head = getChatHeadComponentName(largeHead)
        player.interfaces.sendPlayerHead(name, head)
        player.interfaces.sendAnimation(name, head, expression.id)
        player.interfaces.sendText(name, "title", title ?: player.name)
        sendLines(player, name, lines)
        await<Unit>("player")
    }
}

private fun getChatHeadComponentName(large: Boolean): String {
    return "head${if (large) "_large" else ""}"
}

private fun sendLines(player: Player, name: String, lines: List<String>) {
    for ((index, line) in lines.withIndex()) {
        player.interfaces.sendText(name, "line${index + 1}", line)
    }
}

private fun getInterfaceName(name: String, lines: Int, prompt: Boolean): String {
    return "$name${if (!prompt) "_np" else ""}$lines"
}
package rs.dusk.world.interact.dialogue

import com.github.michaelbull.logging.InlineLogger
import rs.dusk.engine.client.ui.dialogue.Dialogues
import rs.dusk.engine.client.ui.dialogue.Expression
import rs.dusk.engine.client.ui.open
import rs.dusk.engine.entity.character.player.Player

private val logger = InlineLogger()

suspend fun Dialogues.tell(text: String, expression: Expression = Expression.Talking, largeHead: Boolean = false, clickToContinue: Boolean = true, title: String? = null) {
    val lines = text.trimIndent().lines()

    if (lines.size > 4) {
        logger.warn { "Maximum npc chat lines exceeded ${lines.size} for $player" }
        return
    }

    val name = getInterfaceName("npc_chat", lines.size, clickToContinue)

    val npc = npc
    if (npc != null && player.open(name)) {
        val head = getChatHeadComponentName(largeHead)
        player.interfaces.sendNPCHead(name, head, npc.id)
        player.interfaces.sendAnimation(name, head, expression.id)
        player.interfaces.sendText(name, "title", title ?: npc.def.name)
        sendLines(player, name, lines)
        await<Unit>("npc")
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
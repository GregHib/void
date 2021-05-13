package world.gregs.voidps.world.interact.dialogue.type

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.ui.dialogue.DialogueContext
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.ui.sendAnimation
import world.gregs.voidps.engine.client.ui.sendPlayerHead
import world.gregs.voidps.engine.client.ui.sendText
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.AnimationDefinitions
import world.gregs.voidps.utility.get

private val logger = InlineLogger()

suspend fun DialogueContext.player(expression: String, text: String, largeHead: Boolean = false, clickToContinue: Boolean = true, title: String? = null) {
    val lines = text.trimIndent().lines()

    if (lines.size > 4) {
        logger.debug { "Maximum player chat lines exceeded ${lines.size} for $player" }
        return
    }

    val name = getInterfaceName("chat", lines.size, clickToContinue)
    if (player.open(name)) {
        val animationDefs: AnimationDefinitions = get()
        val head = getChatHeadComponentName(largeHead)
        player.interfaces.sendPlayerHead(name, head)
        player.interfaces.sendAnimation(name, head, animationDefs.getId(expression))
        player.interfaces.sendText(name, "title", title ?: player.name)
        sendLines(player, name, lines)
        await<Unit>("chat")
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
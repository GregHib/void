package world.gregs.voidps.world.interact.dialogue.type

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.ui.dialogue.DialogueContext
import world.gregs.voidps.engine.client.ui.dialogue.Expression
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.NPCDefinitions
import world.gregs.voidps.utility.get

private val logger = InlineLogger()

suspend fun DialogueContext.npc(expression: Expression, text: String, largeHead: Boolean = false, clickToContinue: Boolean = true, title: String? = null) {
    npc(npcId, npcName, expression, text, largeHead, clickToContinue, title)
}

suspend fun DialogueContext.npc(id: String, expression: Expression, text: String, largeHead: Boolean = false, clickToContinue: Boolean = true, title: String? = null) {
    val definitions: NPCDefinitions = get()
    val npcId = definitions.getId(id)
    npc(npcId, definitions.getName(npcId), expression, text, largeHead, clickToContinue, title)
}

suspend fun DialogueContext.npc(id: Int, npcName: String, expression: Expression, text: String, largeHead: Boolean = false, clickToContinue: Boolean = true, title: String? = null) {
    val lines = text.trimIndent().lines()

    if (lines.size > 4) {
        logger.debug { "Maximum npc chat lines exceeded ${lines.size} for $player" }
        return
    }

    val name = getInterfaceName("npc_chat", lines.size, clickToContinue)
    if (player.open(name)) {
        val head = getChatHeadComponentName(largeHead)
        player.interfaces.sendNPCHead(name, head, id)
        player.interfaces.sendAnimation(name, head, expression.id)
        player.interfaces.sendText(name, "title", title ?: npcName)
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
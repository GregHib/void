package world.gregs.voidps.world.interact.dialogue.type

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.ui.dialogue.DialogueContext
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.ui.sendAnimation
import world.gregs.voidps.engine.client.ui.sendText
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.AnimationDefinitions
import world.gregs.voidps.engine.entity.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.definition.getComponentOrNull
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.network.encode.npcDialogueHead

private val logger = InlineLogger()

suspend fun DialogueContext.npc(expression: String, text: String, largeHead: Boolean = false, clickToContinue: Boolean = true, title: String? = null) {
    npc(npcId, expression, text, largeHead, clickToContinue, title)
}

suspend fun DialogueContext.npc(npc: String, expression: String, text: String, largeHead: Boolean = false, clickToContinue: Boolean = true, title: String? = null) {
    val lines = text.trimIndent().lines()
    if (lines.size > 4) {
        logger.debug { "Maximum npc chat lines exceeded ${lines.size} for $player" }
        return
    }

    val id = getInterfaceId(lines.size, clickToContinue)
    if (player.open(id)) {
        val npcDefinitions: NPCDefinitions = get()
        val npcDef = npcDefinitions.get(npc)
        val animationDefs: AnimationDefinitions = get()
        val head = getChatHeadComponentName(largeHead)
        sendNPCHead(player, id, head, npcDef.id)
        player.interfaces.sendAnimation(id, head, animationDefs.getIntId("expression_$expression"))
        player.interfaces.sendText(id, "title", title ?: npcDef.name)
        sendLines(player, id, lines)
        await<Unit>("chat")
    }
}

private fun getChatHeadComponentName(large: Boolean): String {
    return "head${if (large) "_large" else ""}"
}

private fun sendLines(player: Player, id: String, lines: List<String>) {
    for ((index, line) in lines.withIndex()) {
        player.interfaces.sendText(id, "line${index + 1}", line)
    }
}

private fun getInterfaceId(lines: Int, prompt: Boolean): String {
    return "npc_chat${if (!prompt) "_np" else ""}$lines"
}

private fun sendNPCHead(player: Player, id: String, component: String, npc: Int) {
    val definitions: InterfaceDefinitions = get()
    val comp = definitions.get(id).getComponentOrNull(component) ?: return
    player.client?.npcDialogueHead(comp["parent", -1], comp.id, npc)
}
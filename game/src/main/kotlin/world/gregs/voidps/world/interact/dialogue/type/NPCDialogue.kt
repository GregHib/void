package world.gregs.voidps.world.interact.dialogue.type

import net.pearx.kasechange.toSnakeCase
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.client.variable.getOrNull
import world.gregs.voidps.engine.data.definition.extra.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.extra.NPCDefinitions
import world.gregs.voidps.engine.data.definition.extra.getComponentOrNull
import world.gregs.voidps.engine.entity.character.mode.Face
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerContext
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.suspend.dialogue.ContinueSuspension
import world.gregs.voidps.network.encode.npcDialogueHead
import world.gregs.voidps.world.interact.dialogue.Expression
import world.gregs.voidps.world.interact.dialogue.sendChat

suspend inline fun <reified E : Expression> PlayerContext.npc(text: String, largeHead: Boolean = false, clickToContinue: Boolean = true, title: String? = null) {
    val expression = E::class.simpleName!!.toSnakeCase()
    npc(expression, text, largeHead, clickToContinue, title)
}

suspend inline fun <reified E : Expression> PlayerContext.npc(npcId: String, text: String, largeHead: Boolean = false, clickToContinue: Boolean = true, title: String? = null) {
    val expression = E::class.simpleName!!.toSnakeCase()
    npc(npcId, expression, text, largeHead, clickToContinue, title)
}

@JvmName("npcExpression")
suspend fun PlayerContext.npc(expression: String, text: String, largeHead: Boolean = false, clickToContinue: Boolean = true, title: String? = null) {
    val target: NPC = player.getOrNull("dialogue_target") ?: throw IllegalArgumentException("No npc specified for dialogue. Please use player.talkWith(npc) or npc(npcId, text).")
    val id = target["transform_id", target.id]
    target.mode = Face(target, player)
    npc(id, expression, text, largeHead, clickToContinue, title)
}

suspend fun PlayerContext.npc(npcId: String, expression: String, text: String, largeHead: Boolean = false, clickToContinue: Boolean = true, title: String? = null) {
    val lines = text.trimIndent().lines()
    check(lines.size <= 4) { "Maximum npc chat lines exceeded ${lines.size} for $player" }
    val id = getInterfaceId(lines.size, clickToContinue)
    check(player.open(id)) { "Unable to open npc dialogue $id for $player" }
    val npcDef = get<NPCDefinitions>().get(npcId)
    val head = getChatHeadComponentName(largeHead)
    sendNPCHead(player, id, head, npcDef.id)
    player.interfaces.sendChat(id, head, expression, title ?: npcDef.name, lines)
    if (clickToContinue) {
        ContinueSuspension()
        player.close(id)
    }
}

private fun getChatHeadComponentName(large: Boolean): String {
    return "head${if (large) "_large" else ""}"
}

private fun getInterfaceId(lines: Int, prompt: Boolean): String {
    return "dialogue_npc_chat${if (!prompt) "_np" else ""}$lines"
}

private fun sendNPCHead(player: Player, id: String, component: String, npc: Int) {
    val definitions: InterfaceDefinitions = get()
    val comp = definitions.get(id).getComponentOrNull(component) ?: return
    player.client?.npcDialogueHead(comp["parent", -1], comp.id, npc)
}
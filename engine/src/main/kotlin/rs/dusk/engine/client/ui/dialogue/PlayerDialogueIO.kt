package rs.dusk.engine.client.ui.dialogue

import com.github.michaelbull.logging.InlineLogger
import rs.dusk.engine.client.ui.open
import rs.dusk.engine.entity.Entity
import rs.dusk.engine.entity.character.npc.NPC
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.update.visual.player.name

class PlayerDialogueIO(private val player: Player) : DialogueIO {

    private val interfaces = player.interfaces

    override fun sendChat(builder: DialogueBuilder): Boolean {
        val lines = builder.lines()
        val target = builder.target

        if (lines.size > 4) {
            logger.warn { "Maximum ${if (target is NPC) "NPC" else "player"} dialogue exceeded $builder for $player" }
            return false
        }

        val name = getChatInterfaceName(target, lines.size, builder.clickToContinue)

        if (player.open(name)) {
            val head = getChatHeadComponentName(builder.large)
            sendEntityHead(name, head, target)
            interfaces.sendAnimation(name, head, builder.expression.id)

            val title = getOverrideOrEntityName(builder.title, target)
            interfaces.sendText(name, "title", title)

            sendLines(name, lines)
            return true
        }
        return false
    }

    private fun getChatHeadComponentName(large: Boolean): String {
        return "head${if (large) "_large" else ""}"
    }

    private fun sendEntityHead(name: String, component: String, entity: Entity) {
        if (entity is NPC) {
            interfaces.sendNPCHead(name, component, entity.id)
        } else {
            interfaces.sendPlayerHead(name, component)
        }
    }

    private fun getOverrideOrEntityName(override: String?, entity: Entity): String = if (override.isNullOrBlank()) {
        getEntityName(entity)
    } else {
        override
    }

    private fun getEntityName(target: Entity): String = when (target) {
        is NPC -> target.def.name
        is Player -> target.name
        else -> ""
    }

    private fun getChatInterfaceName(target: Entity, lines: Int, clickToContinue: Boolean): String {
        val name = getInterfaceName("chat", lines, clickToContinue)
        return if (target is NPC) "npc_$name" else name
    }

    private fun getInterfaceName(name: String, lines: Int, prompt: Boolean): String {
        return "$name${if (!prompt) "_np" else ""}$lines"
    }

    private fun sendLines(name: String, lines: List<String>) {
        for ((index, line) in lines.withIndex()) {
            interfaces.sendText(name, "line${index + 1}", line)
        }
    }

    companion object {
        private val logger = InlineLogger()
    }
}
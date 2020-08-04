package rs.dusk.engine.client.ui.dialogue

import com.github.michaelbull.logging.InlineLogger
import rs.dusk.cache.definition.decoder.ItemDecoder
import rs.dusk.engine.client.ui.open
import rs.dusk.engine.entity.Entity
import rs.dusk.engine.entity.character.npc.NPC
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.update.visual.player.name

class PlayerDialogueIO(private val player: Player, private val itemDecoder: ItemDecoder) : DialogueIO {

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

    override fun sendStatement(builder: DialogueBuilder): Boolean {
        val lines = builder.lines()

        if (lines.size > MAXIMUM_STATEMENT_SIZE) {
            logger.warn { "Maximum statement lines exceeded $builder for $player" }
            return false
        }

        val name = getInterfaceName("message", lines.size, builder.clickToContinue)
        if (player.open(name)) {
            sendLines(name, lines)
            return true
        }
        return false
    }

    override fun sendChoice(builder: DialogueBuilder): Boolean {
        val lines = builder.lines()

        if (lines.size !in CHOICE_LINE_RANGE) {
            logger.warn { "Invalid choice line count $builder for $player" }
            return false
        }

        val title = builder.title
        val multilineTitle = title != null && isMultiline(title)
        val multilineOptions = lines.any { isMultiline(it) }
        val name = getChoiceName(multilineTitle, multilineOptions, lines.size)
        if (player.open(name)) {

            if (title != null) {
                val wide = title.length > APPROXIMATE_WIDE_TITLE_LENGTH
                interfaces.sendVisibility(name, "wide_swords", wide)
                interfaces.sendVisibility(name, "thin_swords", !wide)
                interfaces.sendText(name, "title", title)
            }

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

    private fun isMultiline(string: String): Boolean = string.contains("<br>")

    private fun getChoiceName(multilineTitle: Boolean, multilineOptions: Boolean, lines: Int): String {
        return "multi${if (multilineTitle) "_var" else ""}$lines${if (multilineOptions) "_chat" else ""}"
    }

    companion object {
        private val logger = InlineLogger()
        private const val MAXIMUM_STATEMENT_SIZE = 5
        private val CHOICE_LINE_RANGE = 2..5
        private const val APPROXIMATE_WIDE_TITLE_LENGTH = 30
        private const val STRING_ENTRY_SCRIPT = 108
    }
}
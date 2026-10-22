package content.area.misthalin.tutorial_island

import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.GameMessageFeedback
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.ui.hasMenuOpen
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.data.definition.FontDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.queue.weakQueue
import java.util.WeakHashMap

/** Tutorial instructions cover chat history, so show action refusals as normal statements. */
class TutorialFeedback(val fonts: FontDefinitions) : Script {
    private val pending = WeakHashMap<Player, MutableList<String>>()
    init {
        GameMessageFeedback.listen { player, text, type ->
            if (!player.inTutorial || !player.hasOpen("tutorial_text") || player.dialogue != null || player.hasMenuOpen() || !isFeedback(text, type)) return@listen
            val scheduled = player.queue.contains("tutorial_feedback")
            // A new action may have cancelled the old weak queue; do not revive its messages.
            if (!scheduled) pending[player] = mutableListOf()
            val messages = pending.getOrPut(player) { mutableListOf() }
            // Coalesce related messages without creating a stack of popups.
            if (messages.size < 5) messages.add(text)
            if (!scheduled) {
                player.weakQueue("tutorial_feedback", 1) {
                    val feedback = pending.remove(this) ?: return@weakQueue
                    if (!inTutorial || !hasOpen("tutorial_text") || dialogue != null || hasMenuOpen()) return@weakQueue
                    val font = fonts.get("q8_full")
                    val lines = feedback.flatMap { font.splitLines(it, 470) }.take(5)
                    if (lines.isNotEmpty()) statement(lines.joinToString("\n"))
                }
            }
        }
        // A real dialogue takes precedence over feedback queued just before it opened.
        interfaceOpened("dialogue_*") {
            pending.remove(this)
            queue.clear("tutorial_feedback")
        }
        playerDespawn { pending.remove(this) }
    }
    companion object {
        private val refusal = Regex("^(?:You (?:can't|cannot|don't|do not|need|must|should|have to|haven't|have not|aren't|are not|are unable|are too)|Your inventory|Nothing interesting happens|I (?:can't|cannot) reach)", RegexOption.IGNORE_CASE)
        fun isFeedback(text: String, type: ChatType): Boolean = (type == ChatType.Game || type == ChatType.Engine || type == ChatType.Filter) && refusal.containsMatchIn(text.replace(Regex("<[^>]*>"), "").trim())
    }
}

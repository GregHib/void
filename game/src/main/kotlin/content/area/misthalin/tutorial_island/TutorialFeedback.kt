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

/** Tutorial instructions cover chat history, so show action refusals as normal statements. */
class TutorialFeedback(private val fonts: FontDefinitions) : Script {
    init {
        // No content event observes outgoing messages; this hook preserves their original type.
        GameMessageFeedback.listen { player, text, type -> player.queueFeedback(text, type) }

        interfaceOpened("dialogue_*") { discardFeedback() }

        playerDespawn { discardFeedback() }
    }

    private fun Player.queueFeedback(text: String, type: ChatType) {
        if (!inTutorial || !hasOpen("tutorial_text") || dialogue != null || hasMenuOpen() || !isFeedback(text, type)) {
            return
        }
        val scheduled = queue.contains("tutorial_feedback")
        if (!scheduled) {
            clear("tutorial_feedback_messages")
        }
        val messages = getOrPut("tutorial_feedback_messages") { mutableListOf<String>() }
        if (messages.size < 5) {
            messages.add(text)
        }
        if (!scheduled) {
            weakQueue("tutorial_feedback", 1) { showFeedback() }
        }
    }

    private suspend fun Player.showFeedback() {
        val feedback = get<MutableList<String>>("tutorial_feedback_messages") ?: return
        clear("tutorial_feedback_messages")
        if (!inTutorial || !hasOpen("tutorial_text") || dialogue != null || hasMenuOpen()) {
            return
        }
        val font = fonts.get("q8_full")
        val lines = feedback.flatMap { font.splitLines(it, 470) }.take(5)
        if (lines.isNotEmpty()) {
            statement(lines.joinToString("\n"))
        }
    }

    private fun Player.discardFeedback() {
        clear("tutorial_feedback_messages")
        queue.clear("tutorial_feedback")
    }

    companion object {
        private val refusal = Regex("^(?:You (?:can't|cannot|don't|do not|need|must|should|have to|haven't|have not|aren't|are not|are unable|are too)|Your inventory|Nothing interesting happens|I (?:can't|cannot) reach)", RegexOption.IGNORE_CASE)

        internal fun isFeedback(text: String, type: ChatType): Boolean = (type == ChatType.Game || type == ChatType.Engine || type == ChatType.Filter) && refusal.containsMatchIn(text.replace(Regex("<[^>]*>"), "").trim())
    }
}

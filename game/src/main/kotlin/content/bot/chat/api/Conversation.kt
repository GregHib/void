package content.bot.chat.api

import content.bot.chat.tag.ChatEntity
import content.bot.chat.tag.SlotType
import java.util.EnumMap

/**
 * Short-term memory between one bot and one player.
 */
class Conversation {
    val turns = ArrayDeque<ChatTurn>()
    val topic = EnumMap<SlotType, ChatEntity>(SlotType::class.java)
    var expecting: Expectation? = null
    var annoyance = 0

    // Number of separate conversations, "Nice to meet you." vs "Hello again."
    var sessions = 0
    var lastTime = 0L

    fun add(turn: ChatTurn) {
        turns.addLast(turn)
        if (turns.size > MAX_TURNS) {
            turns.removeFirst()
        }
        for (entity in turn.entities) {
            topic[entity.type] = entity
        }
    }

    fun lastPlayer(ignore: Set<String> = emptySet()): ChatTurn? = turns.lastOrNull { !it.fromBot && it.intent !in ignore }

    fun said(intent: String): Boolean = turns.any { it.fromBot && it.intent == intent }

    /**
     * Starts a new session if it's been more than [SESSION_GAP] since the last message
     */
    fun touch(now: Long) {
        if (sessions == 0 || now - lastTime > SESSION_GAP) {
            turns.clear()
            topic.clear()
            expecting = null
            annoyance = 0
            sessions++
        }
        lastTime = now
    }

    companion object {
        const val MAX_TURNS = 8
        const val SESSION_GAP = 5 * 60_000L
        const val FORGET = 60 * 60_000L
    }
}

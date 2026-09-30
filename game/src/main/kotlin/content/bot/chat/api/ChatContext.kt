package content.bot.chat.api

import com.github.michaelbull.logging.InlineLogger
import content.bot.bot
import content.bot.chat.Conversation
import content.bot.chat.tag.ChatEntity
import content.bot.chat.Expectation
import content.bot.chat.Persona
import content.bot.chat.QuickChatPhrases
import content.bot.chat.tag.SlotType
import content.bot.chat.Utterance
import content.bot.chat.persona
import content.bot.isBot
import world.gregs.voidps.engine.entity.character.player.Player
import java.time.LocalDateTime
import kotlin.random.Random

/**
 * Receiver for [BotChatApi.botChat] handlers, handlers add weighted candidate replies and one is picked.
 */
class ChatContext(
    val bot: Player,
    val speaker: Player,
    val utterance: Utterance,
    val conversation: Conversation,
    private val phrases: QuickChatPhrases,
    val now: LocalDateTime,
    val millis: Long,
    val random: Random,
) {
    val candidates = mutableListOf<Candidate>()
    private var expectation: Expectation? = null

    val persona: Persona = bot.persona

    val intent: String
        get() = utterance.intent

    val activity: BotActivity?
        get() {
            if (!bot.isBot || bot.bot.noTask()) {
                return null
            }
            val produces = bot.bot.frames.peek().behaviour.produces
            val skill = produces.firstOrNull { it.startsWith("skill:") }?.removePrefix("skill:") ?: return null
            return BotActivity(skill, produces.firstOrNull { it.startsWith("item:") }?.removePrefix("item:"))
        }

    /**
     * Entity from this message, otherwise from the conversation topic ("and fishing?" / "what about there?")
     */
    fun slot(type: SlotType): ChatEntity? = utterance.first(type) ?: conversation.topic[type]

    /**
     * Add a candidate reply. [phrase] is the quick chat text with typed placeholders e.g. "Try mining at: <MultipleChoice>."
     * and [args] fill the <MultipleChoice>/<AllItems> placeholders in order. Numeric placeholders fill themselves.
     */
    fun say(phrase: String, vararg args: String, weight: Float = 1f, style: Style = Style.Neutral, then: (() -> Unit)? = null) {
        if (phrase !in phrases) {
            if (missing.add(phrase)) {
                logger.warn { "Unknown quick chat phrase '$phrase' for intent '$intent'." }
            }
            return
        }
        candidates.add(Candidate(phrase, args.toList(), weight * styleWeight(style), intent, expectation, then))
    }

    /**
     * Valid option to not reply at all, real players ignore plenty of messages
     */
    fun silence(weight: Float = 1f) {
        candidates.add(Candidate(null, emptyList(), weight, intent, null, null))
    }

    /**
     * Replies added in [block] wait for a yes/no answer handled by [yes] or [no]
     */
    fun asking(topic: String, yes: ChatContext.() -> Unit, no: ChatContext.() -> Unit, block: ChatContext.() -> Unit) {
        expectation = Expectation(topic, millis + EXPECTATION_TIMEOUT, yes, no)
        block()
        expectation = null
    }

    fun chance(probability: Float) = random.nextFloat() < probability

    private fun styleWeight(style: Style): Float = when (style) {
        Style.Neutral -> 1f
        Style.Slang -> persona.slang * 2
        Style.Formal -> (1 - persona.slang) * 2
        Style.Warm -> persona.friendliness * 2
        Style.Curt -> (1 - persona.friendliness) * 2
    }

    companion object {
        private val logger = InlineLogger("BotChat")
        const val EXPECTATION_TIMEOUT = 60_000L

        /**
         * Phrases used by handlers which don't exist in the cache
         */
        val missing: MutableSet<String> = mutableSetOf()
    }
}

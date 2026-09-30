package content.bot.chat

import com.github.michaelbull.logging.InlineLogger
import content.bot.chat.api.BotChatApi
import content.bot.chat.api.Candidate
import content.bot.chat.api.ChatContext
import content.bot.chat.api.ChatTurn
import content.bot.chat.api.Conversation
import content.bot.chat.api.Persona
import content.bot.chat.api.Utterance
import content.bot.chat.process.ChatProcessor
import content.bot.isBot
import content.social.ignore.ignores
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.update.view.Viewport.Companion.VIEW_RADIUS
import world.gregs.voidps.engine.data.definition.QuickChatPhraseDefinitions
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.network.client.instruction.ChatPublic
import world.gregs.voidps.network.client.instruction.QuickChatPublic
import world.gregs.voidps.type.random
import java.time.LocalDateTime

/**
 * Lets bots reply to nearby players' public chat and quick chat.
 *
 * 1. Player text or quick chat phrase
 * 2. [content.bot.chat.process.ChatProcessor] (intent + entities), or [QuickChatIntents] for known phrases
 * 3. [content.bot.chat.api.Conversation] context
 * 4. [BotChatApi] handlers
 * 4. weighted pick
 * 5. Respond with Quick Chat or typed text
 */
class BotChat(
    phrases: QuickChatPhraseDefinitions,
    model: BotChatModel,
) : Script {

    init {
        chatProcessor = model.chatProcessor
        if (chatProcessor != null) {
            Companion.phrases = QuickChatPhrases(phrases)
            quickChat = QuickChatIntents(phrases, model.quickChat)

            playerDespawn {
                forget(accountName)
            }
        }
    }

    companion object {
        private val logger = InlineLogger("BotChat")
        private const val THRESHOLD = 0.4f
        private const val NEARBY = 6
        private const val RECENT = 30_000L
        private const val NOT_YOU = "not_you"
        private val REPEATABLE = setOf("follow_up", "affirm", "deny", "laugh")

        var chatProcessor: ChatProcessor? = null
            private set
        private var phrases: QuickChatPhrases? = null
        private var quickChat: QuickChatIntents? = null
        private val conversations = HashMap<String, Conversation>()

        /**
         * [speaker] said [text] in public chat, or sent quick chat [phrase], pick a nearby bot to reply
         */
        fun heard(speaker: Player, text: String, phrase: Int = -1) {
            if (speaker.isBot) {
                return
            }
            val processor = chatProcessor ?: return
            val bots = Players.filter { it.isBot && it.tile.within(speaker.tile, VIEW_RADIUS) && !it.ignores(speaker) }
            if (bots.isEmpty()) {
                return
            }
            val utterance = understand(processor, text, phrase)
            val now = System.currentTimeMillis()
            val (bot, addressed) = addressee(speaker, utterance, bots, now) ?: return
            val reply = respond(bot, speaker, utterance, addressed, now, LocalDateTime.now()) ?: return
            // Take a moment to "type"
            val delay = random.nextInt(1, 3) + reply.phrase.length / if (reply.typed) 10 else 25
            World.queue("bot_chat_${bot.accountName}", delay) {
                if (Players.findByAccount(bot.accountName) == null) {
                    return@queue
                }
                if (reply.typed) {
                    bot.instructions.trySend(ChatPublic(reply.phrase, 0))
                } else {
                    bot.instructions.trySend(QuickChatPublic(0, reply.id, reply.data))
                }
                reply.then?.invoke()
            }
        }

        fun reply(bot: Player, speaker: Player, text: String, phrase: Int = -1, addressed: Boolean = true, now: Long = System.currentTimeMillis(), time: LocalDateTime = LocalDateTime.now()): BotChatReply? {
            val processor = chatProcessor ?: return null
            return respond(bot, speaker, understand(processor, text, phrase), addressed, now, time)
        }

        /**
         * Quick chat phrases with a known intent skip the model, the text is still tagged for entities e.g. "What is your level in Mining?"
         */
        private fun understand(processor: ChatProcessor, text: String, phrase: Int): Utterance {
            val utterance = processor.parse(text)
            val intent = quickChat?.intent(phrase) ?: return utterance
            return utterance.copy(intent = intent, confidence = 1f)
        }

        /**
         * Named bots first, then a bot already talking to the speaker, otherwise the closest might chip in.
         * Bots told "not you" stay out of it until they're spoken to by name.
         */
        internal fun addressee(speaker: Player, utterance: Utterance, bots: List<Player>, now: Long): Pair<Player, Boolean>? {
            val normaliser = chatProcessor!!.normaliser
            val named = bots.firstOrNull { bot -> normaliser.tokens(bot.name).all { utterance.mentions(it) } }
            if (named != null) {
                return named to true
            }
            val recent = bots
                .mapNotNull { bot -> conversations[key(bot, speaker)]?.takeUnless { it.dismissed(now) }?.let { bot to it.lastTime } }
                .filter { now - it.second < RECENT }
                .maxByOrNull { it.second }
            if (recent != null) {
                return recent.first to true
            }
            val nearest = bots
                .filter { it.tile.within(speaker.tile, NEARBY) && conversations[key(it, speaker)]?.dismissed(now) != true }
                .minByOrNull { it.tile.distanceTo(speaker.tile) } ?: return null
            return nearest to false
        }

        private fun respond(bot: Player, speaker: Player, utterance: Utterance, addressed: Boolean, now: Long, time: LocalDateTime): BotChatReply? {
            val phrases = phrases ?: return null
            val conversation = conversations.getOrPut(key(bot, speaker)) { Conversation() }
            conversation.start(now)
            val repeated = conversation.lastPlayer()?.text.equals(utterance.text, ignoreCase = true)
            conversation.add(ChatTurn(false, utterance.intent, utterance.entities, utterance.text, now))
            if (utterance.intent == NOT_YOU && utterance.confidence >= THRESHOLD) {
                conversation.dismissed = now
            }
            if (!addressed && random.nextFloat() > Persona.of(bot.accountName).chattiness) {
                return null
            }
            val intent = resolveIntent(utterance, conversation, repeated)
            val context = ChatContext(bot, speaker, utterance.copy(intent = intent), conversation, phrases, time, now, random)
            val expecting = conversation.expecting
            conversation.expecting = null
            when {
                intent == BotChatApi.ANNOYED -> BotChatApi.handle(intent, context)
                expecting != null && now < expecting.expires && intent == "affirm" -> expecting.yes(context)
                expecting != null && now < expecting.expires && intent == "deny" -> expecting.no(context)
                !BotChatApi.handle(intent, context) -> BotChatApi.handle(BotChatApi.UNKNOWN, context)
            }
            val chosen = pick(context.candidates) ?: return null
            conversation.expecting = chosen.expectation
            val phrase = chosen.phrase ?: return null
            val reply = if (chosen.typed) BotChatReply.typed(phrase, chosen.then) else encode(phrases, phrase, chosen) ?: return null
            conversation.add(ChatTurn(true, chosen.intent, emptyList(), phrase, now))
            prune(now)
            return reply
        }

        private fun encode(phrases: QuickChatPhrases, phrase: String, chosen: Candidate): BotChatReply? {
            val id = phrases.id(phrase) ?: return null
            val data = phrases.encode(id, chosen.args)
            if (data == null) {
                logger.debug { "Unable to encode '$phrase' with ${chosen.args}." }
                return null
            }
            return BotChatReply(phrase, chosen.args, id, data, chosen.then)
        }

        private fun resolveIntent(utterance: Utterance, conversation: Conversation, repeated: Boolean): String {
            val rude = repeated || utterance.intent == "insult"
            if (rude) {
                conversation.annoyance++
            }
            if (rude && conversation.annoyance > 1) {
                return BotChatApi.ANNOYED
            }
            if (utterance.confidence < THRESHOLD) {
                return BotChatApi.UNKNOWN
            }
            if (utterance.intent != "follow_up") {
                return utterance.intent
            }
            // "and fishing?" repeats the previous question, the new entity is already in the conversation topic
            return conversation.lastPlayer(REPEATABLE)?.intent ?: BotChatApi.UNKNOWN
        }

        private fun pick(candidates: List<Candidate>): Candidate? {
            val total = candidates.sumOf { it.weight.toDouble() }
            if (total <= 0.0) {
                return null
            }
            var roll = random.nextDouble() * total
            for (candidate in candidates) {
                roll -= candidate.weight
                if (roll <= 0) {
                    return candidate
                }
            }
            return candidates.last()
        }

        private fun key(bot: Player, speaker: Player) = "${bot.accountName}:${speaker.accountName}"

        private fun prune(now: Long) {
            if (conversations.size < 256) {
                return
            }
            conversations.values.removeIf { now - it.lastTime > Conversation.FORGET }
        }

        private fun forget(name: String) {
            conversations.keys.removeIf { it.startsWith("$name:") || it.endsWith(":$name") }
        }

        fun clear() {
            conversations.clear()
        }
    }
}

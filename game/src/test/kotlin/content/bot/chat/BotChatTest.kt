package content.bot.chat

import WorldTest
import content.bot.Bot
import content.bot.FakeBehaviour
import content.bot.behaviour.BehaviourFrame
import content.bot.chat.api.ChatContext
import content.bot.chat.model.IntentTrainer
import content.bot.chat.process.ChatProcessor
import content.bot.chat.tag.ChatEntityTagger
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.test.get
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.configFiles
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.QuickChatPhraseDefinitions
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom
import java.time.LocalDateTime
import kotlin.random.Random

class BotChatTest : WorldTest() {

    private lateinit var bot: Player
    private lateinit var player: Player
    private val time = LocalDateTime.of(2026, 9, 30, 12, 0)

    @BeforeEach
    fun setup() {
        setRandom(Random(0))
        bot = createPlayer(Tile(3222, 3218), "chat_bot") { it["bot"] = Bot(it) }
        player = createPlayer(Tile(3223, 3218), "chatter")
    }

    @AfterEach
    fun teardown() {
        BotChat.clear()
    }

    private fun reply(text: String, now: Long) = BotChat.reply(bot, player, text, now = now, time = time)

    @Test
    fun `Model and entities are loaded`() {
        assertNotNull(BotChat.chatProcessor)
    }

    @Test
    fun `Answer level questions and follow ups`() {
        assertEquals("My Mining level is <SkillLevel>.", reply("whats ur mining lvl", 1_000)?.phrase)
        assertEquals("My Woodcutting level is <SkillLevel>.", reply("and wc?", 2_000)?.phrase)
    }

    @Test
    fun `Advice is for the asker's level`() {
        player.levels.set(Skill.Mining, 20)
        val reply = reply("where should i train mining", 1_000)
        assertEquals("Try mining at: <MultipleChoice>.", reply?.phrase)
        assertTrue(reply!!.args.single() in setOf("varrock_south_east", "varrock_south_west"))
        assertTrue(reply.data.isNotEmpty())
    }

    @Test
    fun `Describe current activity with enum slot`() {
        bot["bot", Bot(bot)]
        (bot.get<Bot>("bot")!!).frames.add(BehaviourFrame(FakeBehaviour(setOf("skill:mining", "item:iron_ore"))))
        val phrases = QuickChatPhrases(get())
        val definitions = get<QuickChatPhraseDefinitions>()
        repeat(20) { attempt ->
            BotChat.clear()
            val reply = reply("what are you doing", attempt * 1_000L) ?: return@repeat
            if (reply.phrase == "I'm mining ore: <MultipleChoice>.") {
                val text = definitions.get(reply.id).buildString(EnumDefinitions.definitions, ItemDefinitions.definitions, reply.data)
                assertEquals("I'm mining ore: iron.", text)
                assertEquals(phrases.id(reply.phrase), reply.id)
                return
            }
        }
        throw AssertionError("Bot never said what it was mining.")
    }

    @Test
    fun `Yes answers the bot's question`() {
        repeat(20) { attempt ->
            BotChat.clear()
            val question = reply("im lost", attempt * 10_000L)
            if (question?.phrase == "Do you need help?") {
                assertEquals("Try looking in the Game Guide.", reply("yes", attempt * 10_000L + 1_000)?.phrase)
                return
            }
        }
        throw AssertionError("Bot never offered help.")
    }

    @Test
    fun `Repeated insults end with ignore`() {
        for (i in 1..10) {
            val then = reply("noob", i * 1_000L)?.then ?: continue
            then.invoke()
            assertTrue(bot.ignores.contains(player.accountName))
            return
        }
        throw AssertionError("Bot never ignored.")
    }

    @Test
    fun `All phrases used exist in quick chat`() {
        val messages = listOf(
            "hi", "bye", "ty", "how are you", "im good", "i died", "lol", "nice armour", "gz", "just got 50 attack",
            "are you a bot", "yes", "no", "noob", "asdf qwerty", "whats ur combat", "how many qp", "what are you doing",
            "where should i train fishing", "where can i get lobster", "how do i get to varrock", "can you help me",
            "follow me", "can i have some food", "wanna trade", "what is your agility level", "where to train thieving",
        )
        for (seed in 0 until 10) {
            setRandom(Random(seed))
            BotChat.clear()
            for ((index, message) in messages.withIndex()) {
                reply(message, index * 1_000L)
            }
        }
        assertTrue(ChatContext.missing.isEmpty(), "Missing phrases: ${ChatContext.missing}")
    }

    /**
     * Guards against examples being changed, or new content names hijacking common words, in ways that hurt understanding
     */
    @Test
    fun `Held out examples are understood`() {
        val processor = BotChat.chatProcessor!!
        val examples = BotChatModel.examples(configFiles().list(Settings["bots.chat.intents"]))
        val random = Random(1)
        val train = LinkedHashMap<String, List<String>>()
        val test = mutableListOf<Pair<String, String>>()
        for ((intent, list) in examples) {
            val shuffled = list.shuffled(random)
            val count = (shuffled.size * 0.15).toInt().coerceAtLeast(1)
            shuffled.take(count).mapTo(test) { it to intent }
            train[intent] = shuffled.drop(count)
        }
        val model = IntentTrainer().train(train, processor.normaliser, processor.entityTagger)
        val held = ChatProcessor(processor.normaliser, processor.entityTagger, model)
        val wrong = test.filter { (text, intent) -> held.parse(text).intent != intent }
        val accuracy = 1.0 - wrong.size.toDouble() / test.size
        assertTrue(accuracy >= 0.8, "Accuracy $accuracy, misclassified: $wrong")
    }

    @Test
    fun `All knowledge table values resolve to quick chat enums`() {
        val phrases = QuickChatPhrases(get())
        val failed = mutableListOf<String>()
        for (row in Tables.get("chat_skills").rows()) {
            val advice = row.string("advice")
            if (advice.isNotEmpty()) {
                val id = phrases.id(advice) ?: throw AssertionError("Unknown phrase $advice")
                for ((_, spot) in Tables.intStrList("chat_skills.${row.rowId}.spots")) {
                    if (phrases.encode(id, listOf(spot)) == null) {
                        failed.add("${row.rowId}: $spot")
                    }
                }
            }
            for ((_, tip) in Tables.intStrList("chat_skills.${row.rowId}.tips")) {
                if (tip !in phrases) {
                    failed.add("${row.rowId}: $tip")
                }
            }
        }
        val go = phrases.id("Go to location: <MultipleChoice>.")!!
        for (key in ChatEntityTagger.locationKeys()) {
            if (phrases.encode(go, listOf(key)) == null) {
                failed.add("location: $key")
            }
        }
        for (row in Tables.get("chat_item_sources").rows()) {
            if (phrases.encode(go, listOf(row.string("location"))) == null) {
                failed.add("item source: ${row.rowId}")
            }
        }
        assertTrue(failed.isEmpty(), "Unresolved: $failed")
    }
}

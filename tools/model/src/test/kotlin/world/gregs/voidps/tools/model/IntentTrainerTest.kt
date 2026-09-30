package world.gregs.voidps.tools.model

import content.bot.chat.BotChatModel
import content.bot.chat.tag.ChatEntityTagger
import content.bot.chat.model.IntentModel
import content.bot.chat.model.IntentTrainer
import content.bot.chat.process.Normaliser
import content.bot.chat.tag.SlotType
import content.bot.chat.process.ChatProcessor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class IntentTrainerTest {

    private val normaliser = Normaliser(
        slang = mapOf("ur" to listOf("your"), "lvl" to listOf("level"), "whats" to listOf("what", "is"), "ty" to listOf("thank", "you")),
        smileys = mapOf(":)" to "smile_face"),
    )

    private fun entityTagger() = ChatEntityTagger(normaliser).apply {
        add(SlotType.Skill, "woodcutting", listOf("woodcutting", "wc"))
        add(SlotType.Skill, "mining", listOf("mining"))
        add(SlotType.Location, "grand_exchange", listOf("Grand Exchange", "ge"))
        add(SlotType.Item, "rune_scimitar", listOf("Rune scimitar", "rune scim"))
        add(SlotType.Item, "shark", listOf("Shark"))
    }

    @Test
    fun `Normalise slang, stretched words and smileys`() {
        assertEquals(listOf("what", "is", "your", "level", "?"), normaliser.tokens("Whats ur LVL??"))
        assertEquals(listOf("hey", "smile_face"), normaliser.tokens("heyyyyy :)"))
    }

    @Test
    fun `Tag multi word, aliased and misspelt entities`() {
        val tagger = entityTagger()
        val tagged = tagger.tag(normaliser.tokens("selling rune scim at the grand exchnge"))
        assertEquals(listOf("selling", "{item}", "at", "the", "{location}"), tagged.tokens)
        assertEquals(listOf("rune_scimitar", "grand_exchange"), tagged.entities.map { it.key })
        assertEquals("woodcutting", tagger.tag(listOf("wc")).entities.single().key)
        assertEquals("mining", tagger.tag(listOf("minnig")).entities.single().key)
    }

    @Test
    fun `Items only match exactly and ordinary words are never fuzzy`() {
        val tagger = entityTagger()
        assertTrue(tagger.tag(listOf("sharc")).entities.isEmpty())
        tagger.protect(listOf("mining"))
        tagger.protect(listOf("minin"))
        assertTrue(tagger.tag(listOf("minin")).entities.isEmpty())
        assertTrue(ChatEntityTagger.withinOneEdit("falador", "faldor"))
        assertFalse(ChatEntityTagger.withinOneEdit("mining", "fishing"))
    }

    private val examples = mapOf(
        "greet" to listOf("hi", "hello", "hey there", "yo", "hiya mate", "good morning"),
        "ask_level" to listOf("what is your mining level", "whats ur wc lvl", "mining level?", "what level is your mining", "your woodcutting level"),
        "thanks" to listOf("ty", "thank you", "thanks a lot", "ty mate", "cheers"),
    )

    @Test
    fun `Train, save and load a model`(@TempDir dir: File) {
        val tagger = entityTagger()
        val model = IntentTrainer(buckets = 1 shl 12, epochs = 20).train(examples, normaliser, tagger, "abc")
        val processor = ChatProcessor(normaliser, tagger, model)
        assertEquals("ask_level", processor.parse("whats ur mining lvl").intent)
        assertEquals("greet", processor.parse("heyyy").intent)

        val file = dir.resolve("model.bin")
        model.write(file)
        assertEquals("abc", IntentModel.fingerprint(file))
        val loaded = IntentModel.read(file)
        assertEquals(model.intents, loaded.intents)
        assertEquals("ask_level", ChatProcessor(normaliser, tagger, loaded).parse("mining level?").intent)
    }

    @Test
    fun `Training is deterministic`() {
        val tagger = entityTagger()
        val trainer = IntentTrainer(buckets = 1 shl 12, epochs = 5)
        val one = trainer.train(examples, normaliser, tagger)
        val two = trainer.train(examples, normaliser, tagger)
        assertTrue(one.input.contentEquals(two.input))
        assertTrue(one.output.contentEquals(two.output))
    }

    @Test
    fun `Fingerprint only changes when training input changes`() {
        val trainer = IntentTrainer()
        val tagger = entityTagger()
        val original = BotChatModel.fingerprint(examples, normaliser, tagger, trainer)
        // Unrelated content doesn't matter
        tagger.add(SlotType.Item, "dragon_scimitar", listOf("Dragon scimitar"))
        assertEquals(original, BotChatModel.fingerprint(examples, normaliser, tagger, trainer))
        // Examples do
        assertNotEquals(original, BotChatModel.fingerprint(examples + ("bye" to listOf("cya")), normaliser, tagger, trainer))
        // As do trainer settings
        assertNotEquals(original, BotChatModel.fingerprint(examples, normaliser, tagger, IntentTrainer(epochs = 10)))
        // And new entities which change how examples are tagged
        tagger.add(SlotType.Location, "morning", listOf("morning"))
        assertNotEquals(original, BotChatModel.fingerprint(examples, normaliser, tagger, trainer))
    }
}

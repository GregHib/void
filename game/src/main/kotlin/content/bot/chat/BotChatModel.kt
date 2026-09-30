package content.bot.chat

import com.github.michaelbull.logging.InlineLogger
import content.bot.chat.model.IntentModel
import content.bot.chat.model.IntentTrainer
import content.bot.chat.process.ChatProcessor
import content.bot.chat.process.Normaliser
import content.bot.chat.tag.ChatEntityTagger
import world.gregs.config.Config
import world.gregs.voidps.engine.data.ConfigFiles
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.QuestDefinitions
import world.gregs.voidps.engine.timedLoad
import java.io.File
import java.security.MessageDigest

/**
 * Builds everything needed to understand player chat.
 *
 * The intent model is cached in `bots.chat.model` along with a fingerprint of its training data.
 * On startup the examples are tagged with the current entity tagger and hashed, if anything which affects training has
 * changed (examples, slang, an entity name hijacking a word in the examples, or trainer settings) the model is retrained.
 */
class BotChatModel {
    private val logger = InlineLogger("BotChat")

    var chatProcessor: ChatProcessor? = null
        private set

    fun load(files: ConfigFiles, quests: QuestDefinitions, trainer: IntentTrainer = IntentTrainer()): BotChatModel {
        val examples = examples(files.list(Settings["bots.chat.intents"]))
        if (examples.isEmpty()) {
            logger.warn { "No bot chat intent examples found, bots won't reply to chat." }
            return this
        }
        timedLoad("bot chat") {
            val normaliser = Normaliser.load()
            val entityTagger = ChatEntityTagger.load(normaliser, quests, vocabulary(normaliser, examples))
            val model = model(examples, normaliser, entityTagger, trainer)
            chatProcessor = ChatProcessor(normaliser, entityTagger, model)
            entityTagger.size
        }
        return this
    }

    private fun model(examples: Map<String, List<String>>, normaliser: Normaliser, entityTagger: ChatEntityTagger, trainer: IntentTrainer): IntentModel {
        val fingerprint = fingerprint(examples, normaliser, entityTagger, trainer)
        val file = File(Settings["bots.chat.model"])
        if (file.exists() && IntentModel.fingerprint(file) == fingerprint) {
            return IntentModel.read(file)
        }
        val start = System.currentTimeMillis()
        val model = trainer.train(examples, normaliser, entityTagger, fingerprint)
        model.write(file)
        logger.info { "Bot chat examples changed, model retrained ${model.intents.size} intents in ${System.currentTimeMillis() - start}ms" }
        return model
    }

    companion object {
        /**
         * Reads `[intent] examples = ["...", ...]` sections
         */
        fun examples(paths: List<String>): Map<String, List<String>> {
            val examples = LinkedHashMap<String, MutableList<String>>()
            for (path in paths) {
                Config.fileReader(path) {
                    while (nextSection()) {
                        val list = examples.getOrPut(section()) { mutableListOf() }
                        while (nextPair()) {
                            when (val key = key()) {
                                "examples" -> while (nextElement()) {
                                    list.add(string())
                                }
                                else -> throw IllegalArgumentException("Unexpected key '$key' in $path")
                            }
                        }
                    }
                }
            }
            return examples
        }

        /**
         * Every word in the training examples, these are never fuzzy matched to entities
         */
        fun vocabulary(normaliser: Normaliser, examples: Map<String, List<String>>): Set<String> = examples.values.flatten().flatMap { normaliser.tokens(it) }.filter { it != "?" }.toSet()

        fun fingerprint(examples: Map<String, List<String>>, normaliser: Normaliser, entityTagger: ChatEntityTagger, trainer: IntentTrainer): String {
            val digest = MessageDigest.getInstance("SHA-256")
            digest.update(trainer.settings.toByteArray())
            for ((intent, list) in examples) {
                digest.update(intent.toByteArray())
                digest.update(0)
                for (example in list) {
                    for (token in entityTagger.tag(normaliser.tokens(example)).tokens) {
                        digest.update(token.toByteArray())
                        digest.update(1)
                    }
                    digest.update(2)
                }
            }
            return digest.digest().joinToString("") { "%02x".format(it) }
        }
    }
}

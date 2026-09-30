package world.gregs.voidps.tools.model

import content.bot.chat.BotChatModel
import content.bot.chat.tag.ChatEntityTagger
import content.bot.chat.model.IntentTrainer
import content.bot.chat.process.Normaliser
import content.bot.chat.tag.SlotType
import content.bot.chat.process.ChatProcessor
import kotlin.random.Random

/**
 * Reports how well the bot chat intent model handles messages it hasn't seen.
 * Holds out 15% of the examples per intent, trains on the rest and lists every mistake.
 *
 * The server retrains the real model itself on startup when the examples change.
 */
object EvaluateBotChat {
    @JvmStatic
    fun main(args: Array<String>) {
        val data = BotChatData()
        val examples = data.examples()
        val tagger = data.entityTagger(examples)
        println("Loaded ${examples.values.sumOf { it.size }} examples for ${examples.size} intents, ${tagger.size} entity names")
        commonEntityWords(data.normaliser, tagger, BotChatModel.vocabulary(data.normaliser, examples))
        val (train, test) = split(examples)
        val model = IntentTrainer().train(train, data.normaliser, tagger)
        val errors = evaluate(ChatProcessor(data.normaliser, tagger, model), test)
        errors.forEach(::println)
    }

    fun split(examples: Map<String, List<String>>, random: Random = Random(1)): Pair<Map<String, List<String>>, Map<String, List<String>>> {
        val train = LinkedHashMap<String, List<String>>()
        val test = LinkedHashMap<String, List<String>>()
        for ((intent, list) in examples) {
            val shuffled = list.shuffled(random)
            val count = (shuffled.size * 0.15).toInt().coerceAtLeast(1)
            test[intent] = shuffled.take(count)
            train[intent] = shuffled.drop(count)
        }
        return train to test
    }

    private fun evaluate(processor: ChatProcessor, test: Map<String, List<String>>): List<String> {
        var correct = 0
        var total = 0
        val errors = mutableListOf<String>()
        for ((intent, list) in test) {
            for (text in list) {
                val utterance = processor.parse(text)
                total++
                if (utterance.intent == intent) {
                    correct++
                } else {
                    errors.add("  '$text' expected $intent got ${utterance.intent} (%.2f)".format(utterance.confidence))
                }
            }
        }
        println("Held-out accuracy: $correct/$total (%.1f%%)".format(correct * 100.0 / total))
        return errors
    }

    /**
     * Training words which get tagged as an item or npc, candidates for the `chat_stop_words` table
     */
    private fun commonEntityWords(normaliser: Normaliser, entityTagger: ChatEntityTagger, vocabulary: Set<String>) {
        val tagged = vocabulary.filter { word ->
            entityTagger.tag(normaliser.tokens(word)).entities.any { it.type == SlotType.Item || it.type == SlotType.Npc }
        }
        if (tagged.isNotEmpty()) {
            println("Training words tagged as items/npcs (consider chat_stop_words): ${tagged.sorted()}")
        }
    }
}

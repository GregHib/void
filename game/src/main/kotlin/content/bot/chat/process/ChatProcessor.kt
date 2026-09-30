package content.bot.chat.process

import content.bot.chat.api.Utterance
import content.bot.chat.model.IntentModel
import content.bot.chat.tag.ChatEntityTagger
import content.bot.chat.tag.Tagged

/**
 * Processes real player messages:
 * 1. [content.bot.chat.process.Normaliser] cleans up slang, abbreviations etc.
 * 2. [content.bot.chat.tag.ChatEntityTagger] separates entity names like items, locations, quests etc.
 * 3. [content.bot.chat.model.IntentModel] passes it through a trained classifier to match to a QuickChat response
 */
class ChatProcessor(val normaliser: Normaliser, val entityTagger: ChatEntityTagger, val model: IntentModel) {

    fun tag(text: String): Tagged = entityTagger.tag(normaliser.tokens(text.take(MAX_LENGTH)))

    fun parse(text: String): Utterance {
        val tagged = tag(text)
        val prediction = model.predict(tagged.tokens)
        return Utterance(text, tagged.tokens, prediction.intent, prediction.confidence, tagged.entities)
    }

    companion object {
        const val MAX_LENGTH = 80
    }
}
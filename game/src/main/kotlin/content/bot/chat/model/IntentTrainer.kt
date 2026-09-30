package content.bot.chat.model

import content.bot.chat.Normaliser
import content.bot.chat.tag.ChatEntityTagger
import kotlin.random.Random

/**
 * Trains the fastText-style [IntentModel] with stochastic gradient descent.
 * Seeded so the same examples always produce the same model.
 */
class IntentTrainer(
    private val dim: Int = 32,
    private val buckets: Int = 1 shl 16,
    private val epochs: Int = 30,
    private val rate: Float = 0.3f,
    private val augment: Int = 6,
    private val seed: Int = 634,
) {
    /**
     * Changes to any setting mean previously trained models are out of date
     */
    val settings: String
        get() = "v$VERSION d$dim b$buckets e$epochs r$rate a$augment s$seed"

    /**
     * @param examples intent -> example messages
     * @param normaliser and [entityTagger] prepare examples exactly as messages are at runtime
     */
    fun train(examples: Map<String, List<String>>, normaliser: Normaliser, entityTagger: ChatEntityTagger, fingerprint: String = ""): IntentModel {
        val random = Random(seed)
        val intents = examples.keys.toList()
        val data = ArrayList<Pair<IntArray, Int>>()
        for ((label, intent) in intents.withIndex()) {
            for (example in examples.getValue(intent)) {
                val tokens = normaliser.tokens(example)
                data.add(features(tokens, entityTagger) to label)
                repeat(augment) {
                    data.add(features(noise(tokens, random), entityTagger) to label)
                }
            }
        }
        val bound = 1f / dim
        val model = IntentModel(intents, fingerprint, dim, buckets, FloatArray(buckets * dim) { (random.nextFloat() * 2 - 1) * bound }, FloatArray(intents.size * dim))
        val touched = BooleanArray(buckets)
        val total = epochs * data.size
        var step = 0
        for (epoch in 0 until epochs) {
            data.shuffle(random)
            for ((features, label) in data) {
                // Linearly decaying learning rate
                update(model, features, label, rate * (1f - step++.toFloat() / total))
                for (feature in features) {
                    touched[feature] = true
                }
            }
        }
        // Unused buckets are random noise, zeroing them makes the saved model compress to almost nothing
        for (bucket in 0 until buckets) {
            if (!touched[bucket]) {
                model.input.fill(0f, bucket * dim, (bucket + 1) * dim)
            }
        }
        return model
    }

    /**
     * One step of softmax cross-entropy gradient descent
     */
    private fun update(model: IntentModel, features: IntArray, label: Int, rate: Float) {
        if (features.isEmpty()) {
            return
        }
        val hidden = model.hidden(features)
        val probabilities = model.probabilities(features, hidden)
        val gradient = FloatArray(dim)
        for (i in model.intents.indices) {
            val step = rate * ((if (i == label) 1f else 0f) - probabilities[i])
            val offset = i * dim
            for (d in 0 until dim) {
                gradient[d] += step * model.output[offset + d]
                model.output[offset + d] += step * hidden[d]
            }
        }
        val scale = 1f / features.size
        for (feature in features) {
            val offset = feature * dim
            for (d in 0 until dim) {
                model.input[offset + d] += gradient[d] * scale
            }
        }
    }

    private fun features(tokens: List<String>, entityTagger: ChatEntityTagger): IntArray = Features.hash(entityTagger.tag(tokens).tokens, buckets)

    /**
     * Imitate how players type: typos, dropped words and missing question marks
     */
    private fun noise(tokens: List<String>, random: Random): List<String> {
        val output = ArrayList<String>(tokens.size)
        for (token in tokens) {
            if (token == "?" && random.nextFloat() < 0.5f) {
                continue
            }
            if (tokens.size > 3 && random.nextFloat() < 0.08f) {
                continue
            }
            output.add(if (token.length > 3 && random.nextFloat() < 0.15f) typo(token, random) else token)
        }
        return output
    }

    private fun typo(word: String, random: Random): String {
        val i = random.nextInt(word.length - 1)
        return when (random.nextInt(4)) {
            0 -> word.removeRange(i, i + 1)
            1 -> word.substring(0, i) + word[i + 1] + word[i] + word.substring(i + 2)
            2 -> word.substring(0, i) + word[i] + word.substring(i)
            else -> word.substring(0, i) + ('a' + random.nextInt(26)) + word.substring(i + 1)
        }
    }

    companion object {
        const val VERSION = 1
    }
}

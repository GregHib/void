package content.bot.chat.model

import kotlin.text.iterator

/**
 * Feature hashing. Word unigrams + bigrams keep word order ("are you" vs "you are", "your level" vs "my level")
 * and character 3-5 grams put typos close to their correct spelling.
 */
object Features {
    fun hash(tokens: List<String>, buckets: Int): IntArray {
        val features = ArrayList<Int>(tokens.size * 12)
        var previous = "<s>"
        for (token in tokens) {
            features.add(bucket('w', token, buckets))
            features.add(bucket('b', "$previous $token", buckets))
            previous = token
            if (token.startsWith('{') || token.length < 3) {
                continue
            }
            val padded = "<$token>"
            for (n in 3..5) {
                for (start in 0..padded.length - n) {
                    features.add(bucket('c', padded.substring(start, start + n), buckets))
                }
            }
        }
        features.add(bucket('b', "$previous </s>", buckets))
        return features.toIntArray()
    }

    private fun bucket(prefix: Char, value: String, buckets: Int): Int {
        // FNV-1a
        var hash = (0x811C9DC5.toInt() xor prefix.code) * 0x01000193
        for (char in value) {
            hash = (hash xor char.code) * 0x01000193
        }
        return (hash ushr 1) % buckets
    }
}

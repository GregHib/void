package content.bot.chat.model

import content.bot.chat.Prediction
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.math.exp

/**
 * IntentModel fastText-style classifier: hashed word, bigram and character n-gram features are averaged into a
 * small dense vector which a linear layer maps to "intent" probabilities.
 *
 * Trained by [IntentTrainer] on startup when the examples change, inference is a few thousand float operations.
 */
class IntentModel(
    val intents: List<String>,
    val fingerprint: String,
    val dim: Int,
    val buckets: Int,
    val input: FloatArray,
    val output: FloatArray,
) {
    fun predict(tokens: List<String>): Prediction {
        val probabilities = probabilities(Features.hash(tokens, buckets))
        var best = 0
        for (i in probabilities.indices) {
            if (probabilities[i] > probabilities[best]) {
                best = i
            }
        }
        return Prediction(intents[best], probabilities[best])
    }

    fun hidden(features: IntArray): FloatArray {
        val hidden = FloatArray(dim)
        if (features.isEmpty()) {
            return hidden
        }
        for (feature in features) {
            val offset = feature * dim
            for (d in 0 until dim) {
                hidden[d] += input[offset + d]
            }
        }
        val scale = 1f / features.size
        for (d in 0 until dim) {
            hidden[d] *= scale
        }
        return hidden
    }

    fun probabilities(features: IntArray, hidden: FloatArray = hidden(features)): FloatArray {
        val scores = FloatArray(intents.size)
        var max = Float.NEGATIVE_INFINITY
        for (i in intents.indices) {
            var sum = 0f
            val offset = i * dim
            for (d in 0 until dim) {
                sum += output[offset + d] * hidden[d]
            }
            scores[i] = sum
            if (sum > max) {
                max = sum
            }
        }
        var total = 0f
        for (i in scores.indices) {
            scores[i] = exp(scores[i] - max)
            total += scores[i]
        }
        for (i in scores.indices) {
            scores[i] /= total
        }
        return scores
    }

    /**
     * Gzipped half-precision floats. Only feature buckets used in training are stored, the rest are zero.
     */
    fun write(file: File) {
        file.parentFile?.mkdirs()
        val used = (0 until buckets).filter { bucket -> (0 until dim).any { input[bucket * dim + it] != 0f } }
        val buffer = ByteBuffer.allocate(used.size * (4 + dim * 2) + output.size * 2)
        for (bucket in used) {
            buffer.putInt(bucket)
            for (d in 0 until dim) {
                buffer.putShort(java.lang.Float.floatToFloat16(input[bucket * dim + d]))
            }
        }
        for (value in output) {
            buffer.putShort(java.lang.Float.floatToFloat16(value))
        }
        DataOutputStream(GZIPOutputStream(file.outputStream().buffered())).use { out ->
            out.writeInt(VERSION)
            out.writeUTF(fingerprint)
            out.writeInt(dim)
            out.writeInt(buckets)
            out.writeInt(intents.size)
            for (intent in intents) {
                out.writeUTF(intent)
            }
            out.writeInt(used.size)
            out.write(buffer.array())
        }
    }

    companion object {
        const val VERSION = 3

        fun fingerprint(file: File): String? = DataInputStream(GZIPInputStream(file.inputStream())).use { input ->
            if (input.readInt() != VERSION) null else input.readUTF()
        }

        fun read(file: File): IntentModel = DataInputStream(GZIPInputStream(file.inputStream(), 65536)).use { input ->
            val version = input.readInt()
            require(version == VERSION) { "Unsupported intent model version $version." }
            val fingerprint = input.readUTF()
            val dim = input.readInt()
            val buckets = input.readInt()
            val intents = List(input.readInt()) { input.readUTF() }
            val used = input.readInt()
            val buffer = ByteBuffer.wrap(input.readAllBytes())
            val weights = FloatArray(buckets * dim)
            repeat(used) {
                val offset = buffer.getInt() * dim
                for (d in 0 until dim) {
                    weights[offset + d] = java.lang.Float.float16ToFloat(buffer.getShort())
                }
            }
            val output = FloatArray(intents.size * dim) { java.lang.Float.float16ToFloat(buffer.getShort()) }
            IntentModel(intents, fingerprint, dim, buckets, weights, output)
        }
    }
}

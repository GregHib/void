package world.gregs.voidps.engine.utility

import kotlin.random.Random

/**
 * Discrete probability distribution
 * @param list List of values and weights; non-positive weights will be ignored, weights don't have to add up to 1
 * @param invert Whether the weights should be inverted
 */
class Distribution<T : Any>(
    list: List<Pair<T, Double>>,
    invert: Boolean = false,
    private val random: Random = Random
) {

    /**
     * @param array collection of values to be weighted by occurrence
     */
    constructor(array: Array<T>, invert: Boolean = false, random: Random = Random) : this(
        array
            .groupBy { it }
            .map { it.key to it.value.size.toDouble() },
        invert,
        random
    )

    private val sorted = list.filter { it.second > 0 }.sortedBy { it.second }.toTypedArray()
    private val cumulativeProbability: DoubleArray

    init {
        val values = sorted.map { it.second }.toDoubleArray()
        val normalized = normalizedArray(values)
        cumulativeProbability = cumulativeArray(normalized, invert)
    }

    @Suppress("UNCHECKED_CAST")
    fun sample(size: Int): Array<Any?> {
        check(size > 0) { "Sample size must be positive." }
        return Array(size) { sample() }
    }

    fun sample(): T? {
        if (cumulativeProbability.isEmpty()) {
            return null
        }
        val random = random.nextDouble()
        var index = cumulativeProbability.binarySearch(random)
        if (index < 0) {
            index = -index - 1
            return sorted[index].first
        }
        return sorted.last().first
    }

    private fun normalizedArray(array: DoubleArray): DoubleArray {
        val total = array.sum()
        return array.map { it / total }.toDoubleArray()
    }

    private fun cumulativeArray(array: DoubleArray, inverse: Boolean): DoubleArray {
        val cumulative = DoubleArray(array.size)
        var sum = 0.0
        for (index in array.indices) {
            sum += array[if (inverse) array.lastIndex - index else index]
            cumulative[index] = sum
        }
        return cumulative
    }
}

@JvmName("weightedIntSample")
fun <T : Any> weightedSample(list: List<Pair<T, Int>>, invert: Boolean = false): T? =
    Distribution(list.map { it.first to it.second.toDouble() }, invert).sample()

@JvmName("weightedDoubleSample")
fun <T : Any> weightedSample(list: List<Pair<T, Double>>, invert: Boolean = false): T? =
    Distribution(list, invert).sample()

inline fun <reified T : Any> weightedSample(list: List<T>, invert: Boolean = false): T? =
    Distribution(list.toTypedArray(), invert).sample()

inline fun <reified T : Any> weightedSample(array: Array<T>, invert: Boolean = false): T? =
    Distribution(array, invert).sample()
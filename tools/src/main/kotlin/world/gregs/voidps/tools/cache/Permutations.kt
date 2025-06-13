package world.gregs.voidps.tools.cache

import kotlin.math.pow

class Permutations {

    private val builder = IntArray(1000)
    private var index = 0
    private var hash = 0

    fun combination(start: Int, arr: Array<IntArray>, size: Int, length: Int, separatorGroups: List<IntArray>, affixGroups: List<Pair<IntArray, IntArray>>, block: (Int, IntArray, Int) -> Unit) {
        for ((prefix, suffix) in affixGroups) {
            val groups = if (prefix.isNotEmpty()) separatorGroups.filter { s -> s.all { c -> c == -1 || c == UNDERSCORE } } else separatorGroups
            for (separators in groups) {
                var count = start
                hash = 0
                index = 0
                for (element in prefix) {
                    hash = 31 * hash + element
                    builder[index++] = element
                }
                for (i in 0 until length) {
                    val part = arr[count % size]
                    for (element in part) {
                        hash = 31 * hash + element
                        builder[index++] = element
                    }
                    val separator = separators.getOrNull(i) ?: -1
                    if (i != length - 1 && separator != -1) {
                        hash = 31 * hash + separator
                        builder[index++] = separator
                    }
                    count /= size
                }
                for (element in suffix) {
                    hash = 31 * hash + element
                    builder[index++] = element
                }
                block.invoke(hash, builder, index)
            }
        }
    }

    fun print(arr: List<String>, length: Int, separatorGroups: List<IntArray>, affixGroups: List<Pair<IntArray, IntArray>>, block: (Int, IntArray, Int) -> Unit) {
        val data = arr.map { s -> s.map { it.code }.toIntArray() }.toTypedArray()
        val size = data.size
        for (i in 0 until size.toDouble().pow(length.toDouble()).toInt()) {
            combination(i, data, size, length, separatorGroups, affixGroups, block)
        }
    }

    companion object {
        private const val UNDERSCORE = '_'.code

        @JvmStatic
        fun main(args: Array<String>) {
            println(' '.code)
            println("status1".hashCode())
            println("status2".hashCode())
            println("status3".hashCode())
//        val arr = listOf("1", "2", "3")

            // function call
//        print(arr, 3) { hash, array, length ->
//            println("$hash ${array.take(length).map { it.toChar() }.toCharArray().concatToString()}")
//        }
        }
    }
}

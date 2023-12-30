package world.gregs.voidps.tools

import com.displee.cache.CacheLibrary
import java.io.File
import kotlin.math.pow

object HashCodeMatcher {
    @JvmStatic
    fun main(args: Array<String>) {
        val lib = CacheLibrary.create(property("cachePath"))
        val indices = lib.indices().filter { idx -> idx.archives().any { it.hashName != 0 } }.map { it.id }
        val folder = File("./temp/hashes/")
        for(index in indices) {
            folder.resolve("$index").mkdirs()
        }
        val hashes = indices.flatMap { index -> lib.index(index).archives().filter { it.hashName != 0 }.map { it.hashName } }.toSet()
        val indexMap = indices.flatMap { index -> lib.index(index).archives().filter { it.hashName != 0 }.map { it.hashName to index } }.toMap()
        val alpha = "abcdefghijklmnopqrstuvwxyz0123456789_"
        val seq = alpha.map { it.code }.toIntArray()
        var hash: Int
        for (length in 1..10) {
            val builder = IntArray(length)
            val pos = IntArray(length)
            val total = alpha.length.toDouble().pow(length.toDouble()).toLong()
            for (count in 0 until total) {
                for (x in 0 until length) {
                    if (pos[x] == seq.size) {
                        pos[x] = 0
                        if (x + 1 < length) {
                            pos[x + 1]++
                        }
                    }
                    builder[x] = seq[pos[x]]
                }
                pos[0]++
                hash = 0
                for (i in 0 until length) {
                    hash = 31 * hash + builder[i]
                }
                if (hashes.contains(hash)) {
                    val string = builder.map { it.toChar() }.toCharArray().concatToString()
                    val index = indexMap.getValue(hash)
                    folder.resolve("$index").resolve("${hash}.txt").appendText("$string\n")
                    println("Match: '$string' $hash len=$length count=$count")
                }
            }
        }
    }
}
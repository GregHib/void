package world.gregs.voidps.tools.cache

import com.displee.cache.CacheLibrary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import world.gregs.voidps.cache.Index
import world.gregs.voidps.tools.property
import java.io.File
import kotlin.math.pow

object HashCodeMatcher {


    @JvmStatic
    fun main(args: Array<String>) {
        dumpHashes()
        val keywords = File("./tools/src/main/resources/keywords.txt").readLines().toMutableList()
        val separators = separators()
        val affixes = affixes()
        val hashes = File("./temp/hashes/hashes.txt").readLines().associate {
            val parts = it.split(", ")
            parts.first().toInt() to Triple(parts[1].toInt(), parts[2].toInt(), parts[3].toInt())
        }
        val knownHashes = File("./temp/names.tsv").readLines().associate {
            val parts = it.split("\t")
            parts[3].toInt() to parts[4]
        }.toMutableMap()
        println("Maximum permutations: ${(1..5).sumOf { affixes[it].size * separators[it].size.toLong() * (it.toDouble().pow(keywords.size).toLong()) }}")
        val debug = File("./temp/hashes/debug.tsv")
        val found = File("./temp/hashes/found.tsv")
        found.delete()
        debug.delete()
        runBlocking {
            supervisorScope {
                for (i in 1..5) {
                    launch(Dispatchers.IO) {
                        val findings = mutableListOf<String>()
                        Permutations().print(keywords, i, separators[i - 1], affixes[i - 1]) { hash, array, length -> // comma
                            val data = hashes[hash] ?: return@print
                            if (data.first != Index.CLIENT_SCRIPTS && array[0] == 91) { // [
                                return@print
                            }
                            val string = array.take(length).map { it.toChar() }.toCharArray().concatToString()
                            if (!knownHashes.containsKey(hash)) {
                                knownHashes[hash] = string
                                println("Found $i $hash $string")
                                findings.add("${data.first}\t${data.second}\t${data.third}\t${hash}\t${string}\n")
                                if (findings.size > 10_000) {
                                    for (find in findings) {
                                        found.appendText(find)
                                    }
                                    findings.clear()
                                }
                            } else {
                                val known = knownHashes[hash]
                                if (known != string) {
                                    debug.appendText("$i\t$hash\t$string\t$known\n")
                                    println("Duplicate $i $hash $string ${knownHashes[hash]}")
                                }
                            }
                        }
                        for (find in findings) {
                            found.appendText(find)
                        }
                    }
                }
            }
        }
        File("./temp/hashes/all.csv").writeText(knownHashes.toList().joinToString("\n") { "${it.first}, ${it.second}" })
    }

    private fun affixes(): Array<List<Pair<IntArray, IntArray>>> {
        val process = "[proc,".toCharArray().map { it.code }.toIntArray() to intArrayOf(']'.code)
        val clientscript = "[clientscript,".toCharArray().map { it.code }.toIntArray() to intArrayOf(']'.code)
        val empty = intArrayOf() to intArrayOf()
        val all = listOf(process, clientscript, empty)
        return arrayOf(all, all, all, all, all, all)
    }

    private fun separators(): Array<List<IntArray>> {
        val nothing = -1
        val underscore = '_'.code
        val dot = '.'.code
        val comma = ','.code
        val space = ' '.code
        return arrayOf(
            listOf(
                intArrayOf(nothing)
            ),
            listOf(
                // 2
                intArrayOf(nothing),
                intArrayOf(underscore),
                intArrayOf(dot),
                intArrayOf(comma),
                intArrayOf(space),
            ),
            listOf(
                // 3
                intArrayOf(nothing, nothing),
                intArrayOf(nothing, underscore),
                intArrayOf(underscore, nothing),
                intArrayOf(underscore, underscore),
                intArrayOf(nothing, dot),
                intArrayOf(nothing, comma),
                intArrayOf(underscore, comma),
                intArrayOf(space, space),
                intArrayOf(nothing, space),
                intArrayOf(space, nothing),
            ),
            listOf(
                // 4
                intArrayOf(nothing, nothing, nothing),
                intArrayOf(nothing, underscore, underscore),
                intArrayOf(underscore, nothing, underscore),
                intArrayOf(underscore, underscore, nothing),
                intArrayOf(nothing, nothing, underscore),
                intArrayOf(nothing, underscore, nothing),
                intArrayOf(underscore, nothing, nothing),
                intArrayOf(underscore, underscore, underscore),
                intArrayOf(nothing, nothing, dot),
                intArrayOf(nothing, nothing, comma),
                intArrayOf(underscore, underscore, comma),
                intArrayOf(underscore, nothing, comma),
                intArrayOf(space, space, space),
                intArrayOf(nothing, space, space),
                intArrayOf(space, nothing, space),
                intArrayOf(space, space, nothing),
                intArrayOf(space, nothing, nothing),
                intArrayOf(nothing, space, nothing),
                intArrayOf(nothing, nothing, space),
            ),
            listOf(
                // 5
                intArrayOf(nothing, nothing, nothing, nothing),
                intArrayOf(nothing, underscore, underscore, underscore),
                intArrayOf(underscore, nothing, underscore, underscore),
                intArrayOf(underscore, underscore, nothing, underscore),
                intArrayOf(underscore, underscore, underscore, nothing),
                intArrayOf(nothing, nothing, underscore, underscore),
                intArrayOf(nothing, underscore, nothing, underscore),
                intArrayOf(nothing, underscore, underscore, nothing),
                intArrayOf(underscore, nothing, underscore, nothing),
                intArrayOf(underscore, underscore, nothing, nothing),
                intArrayOf(underscore, nothing, nothing, nothing),
                intArrayOf(nothing, underscore, nothing, nothing),
                intArrayOf(nothing, nothing, underscore, nothing),
                intArrayOf(nothing, nothing, nothing, underscore),
                intArrayOf(underscore, underscore, underscore, underscore),
                intArrayOf(nothing, nothing, nothing, dot),
                intArrayOf(nothing, nothing, nothing, comma),
                intArrayOf(underscore, nothing, nothing, comma),
                intArrayOf(nothing, underscore, nothing, comma),
                intArrayOf(nothing, nothing, underscore, comma),
                intArrayOf(underscore, underscore, nothing, comma),
                intArrayOf(underscore, nothing, underscore, comma),
                intArrayOf(nothing, underscore, underscore, comma),
                intArrayOf(underscore, underscore, underscore, comma),
                intArrayOf(space, space, space, space),
                intArrayOf(nothing, space, space, space),
                intArrayOf(space, nothing, space, space),
                intArrayOf(space, space, nothing, space),
                intArrayOf(space, space, space, nothing),
                intArrayOf(nothing, nothing, space, space),
                intArrayOf(nothing, space, nothing, space),
                intArrayOf(nothing, space, space, nothing),
                intArrayOf(space, nothing, space, nothing),
                intArrayOf(space, space, nothing, nothing),
                intArrayOf(space, nothing, nothing, nothing),
                intArrayOf(nothing, space, nothing, nothing),
                intArrayOf(nothing, nothing, space, nothing),
                intArrayOf(nothing, nothing, nothing, space),
                intArrayOf(nothing, nothing, nothing, nothing),
            ),
            listOf(
                // 6
                intArrayOf(nothing, nothing, nothing, nothing, nothing),
                intArrayOf(underscore, underscore, underscore, underscore, underscore),
                intArrayOf(space, space, space, space, space),
            )
        )
    }

    private fun dumpHashes() {
        val lib = CacheLibrary.create(property("cachePath"))
        val hashes = File("./temp/hashes/hashes.txt")
        hashes.delete()
        for (index in lib.indices()) {
            for (archive in index.archives()) {
                if (archive.hashName != 0) {
                    hashes.appendText("${archive.hashName}, ${index.id}, ${archive.id}, ${-1}\n")
                }
                for (file in archive.files()) {
                    if (file.hashName != 0) {
                        hashes.appendText("${file.hashName}, ${index.id}, ${archive.id}, ${file.id}\n")
                    }
                }
            }
        }
    }
}
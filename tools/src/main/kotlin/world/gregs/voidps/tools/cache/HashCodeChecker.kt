package world.gregs.voidps.tools.cache

import java.io.File

object HashCodeChecker {

    private val matches = mutableMapOf<Int, String>()
    private lateinit var known: Map<Int, String?>
    private val input = File("./temp/hashes/check.tsv")
    private const val WRITE_CHANGES = false

    @JvmStatic
    fun main(args: Array<String>) {
        val checkList = input.readLines()
            .map { it.split("\t").last() }

        val file = File("./temp/hashes/634-cache-hash-names.tsv")
        known = file.readLines()
            .associate {
                val parts = it.split("\t")
                val string = parts.getOrNull(4)
                parts[3].toInt() to if (string.isNullOrBlank()) null else string
            }

        for ((key, value) in known) {
            if (!value.isNullOrBlank() && value.hashCode() != key) {
                println("Invalid $key $value - ${value.hashCode()}")
            }
        }

        for (check in checkList) {
            val hash = check.hashCode()
            if (known.containsKey(hash) && known[hash] == null) {
                println("Found $hash '$check'")
                matches[hash] = check
            }
        }
        println("Checked ${checkList.size} against ${known.size} found ${matches.size} matches.")
        if (WRITE_CHANGES) {
            file.writeText(file.readLines().joinToString("\n") { line ->
                val parts = line.split("\t").toMutableList()
                if (parts.getOrNull(4).isNullOrBlank()) {
                    val id = parts[3].toInt()
                    if (matches.containsKey(id)) {
                        if (parts.size == 4) {
                            parts.add(matches.getValue(id))
                        } else {
                            parts[4] = matches.getValue(id)
                        }
                    }
                }
                parts.joinToString("\t")
            })
            println("Changes written to file.")
        }
    }
}
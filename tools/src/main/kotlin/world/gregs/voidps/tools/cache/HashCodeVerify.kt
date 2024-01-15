package world.gregs.voidps.tools.cache

import java.io.File

object HashCodeVerify {

    @JvmStatic
    fun main(args: Array<String>) {
        val file = File("./temp/hashes/hashes-modified.tsv")
        val known = file.readLines()
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

        file.readLines().map { it.split("\t") }.groupBy { it[0] }.forEach { (index, lines) ->
            val found = lines.count { !it.getOrNull(4).isNullOrBlank() }

            println("""
                [tr]
                	[td]$index[/td]
                	[td][/td]
                	[td]$found[/td]
                	[td]${lines.size}[/td]
                	[td]${String.format("%.2f", found / lines.size.toDouble() * 100.0)}%[/td]
                [/tr]
            """.trimIndent())
        }
    }
}
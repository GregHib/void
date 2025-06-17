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

        println(
            """
            [table="width: 500, class: full_border"]
            [tr]
            	[td]Index id[/td]
            	[td]Index name[/td]
            	[td]Identified[/td]
            	[td]Total[/td]
            	[td]Percentage[/td]
            [/tr]
            """.trimIndent(),
        )
        file.readLines().map { it.split("\t") }.groupBy { it[0] }.forEach { (index, lines) ->
            val found = lines.count { !it.getOrNull(4).isNullOrBlank() }

            println(
                """
                [tr]
                	[td]$index[/td]
                	[td]${names[index.toInt()]}[/td]
                	[td]$found[/td]
                	[td]${lines.size}[/td]
                	[td]${String.format("%.2f", found / lines.size.toDouble() * 100.0)}%[/td]
                [/tr]
                """.trimIndent(),
            )
        }
        println("[/table]")
    }

    private val names = mapOf(
        3 to "Interfaces & components",
        5 to "Maps",
        6 to "Music",
        8 to "Sprites",
        10 to "Huffman",
        12 to "Client scripts",
        13 to "Font metrics",
        23 to "World map",
        30 to "Native libraries",
        31 to "Shaders",
        33 to "Game tips",
        34 to "loading fonts and sprites",
    )
}

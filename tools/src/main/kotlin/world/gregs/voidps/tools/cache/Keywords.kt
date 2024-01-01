package world.gregs.voidps.tools.cache

import java.io.File

object Keywords {

    @JvmStatic
    fun main(args: Array<String>) {
        val file = File("./tools/src/main/resources/keywords.txt")
        val set = file.readLines().map { it.lowercase() }.toMutableSet()
        file.writeText(set.sorted().joinToString("\n"))
    }
}
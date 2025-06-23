package world.gregs.voidps.tools.cache

import java.io.File

object Keywords {

    operator fun invoke(path: String = "./tools/src/main/resources/keywords.txt"): Set<String> {
        val file = File(path)
        val set = file.readLines().toMutableSet()
        val combo = set.filter { it.contains("_") }
        for (string in combo) {
            set.addAll(string.split("_"))
            set.add(string.split("_").map { it.first() }.joinToString(""))
            set.add(string.replace("_", ""))
            set.add(string.replace("_", " "))
        }
        return set
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val file = File("./tools/src/main/resources/keywords.txt")
        val set = file.readLines().toMutableSet()
        file.writeText(set.sorted().joinToString("\n"))
    }
}

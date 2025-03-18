package world.gregs.voidps.tools.definition

import org.jsoup.Jsoup
import java.io.File
import java.io.FileWriter
import java.util.*

object YamlInjector {
    @JvmStatic
    fun main(args: Array<String>) {
        val doc = Jsoup.parse(File("./Smithing - OSRS Wiki.htm").readText())
        val map = mutableMapOf<String, Pair<Int, Double>>()
        for ((index, table) in doc.select(".wikitable").drop(1).take(7).withIndex()) {
            if (index == 3) {
                continue
            }
            for (row in table.select("tr").drop(1)) {
                val columns = row.select("td")
                val level = columns[0].text().toInt()
                val name = columns[1].select("a").attr("title")
                val regex = "× ([0-9]+)".toPattern()
                val matcher = regex.matcher(columns[2].text())
                val item = if (matcher.find()) matcher.group(1) else "1"
                val xp = columns[3].text().toDouble()
                val members = columns[11].text()
                val key = name.lowercase()
                    .replace(" ", "_")
                    .replace("(", "")
                    .replace(")", "")
                    .replace("_axe", "_hatchet")
                map[key] = level to xp
                println("$key - $level - $name - $item - $xp - $members")
            }
        }
        val output = FileWriter("items.toml")
        val queue = LinkedList(File("./data/definitions/items.toml").readLines())
        while (queue.isNotEmpty()) {
            val line = queue.pop()
            output.write("${line}\n")
            if (!line.startsWith(" ")) {
                val name = line.removeSuffix(":")
                val value = map[name]
                if (value != null) {
                    // Place before the first string value
                    while (!queue.peek().contains("\"")) {
                        output.write("${queue.pop()}\n")
                    }
                    output.write("  smithing:\n")
                    output.write("    level: ${value.first}\n")
                    output.write("    xp: ${value.second}\n")
                }
            }
        }
    }
}
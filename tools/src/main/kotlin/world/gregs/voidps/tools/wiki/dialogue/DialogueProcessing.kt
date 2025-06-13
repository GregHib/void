package world.gregs.voidps.tools.wiki.dialogue

import org.jsoup.Jsoup
import world.gregs.yaml.Yaml
import world.gregs.yaml.write.YamlWriterConfiguration
import java.io.File

/**
 * Processes raw html files into yaml
 */
object DialogueProcessing {

    @JvmStatic
    fun main(args: Array<String>) {
        val folder = File("./temp/chisel/dialogue/")
        npcs(folder)
        content(folder)
    }

    private fun content(folder: File) {
        val yamlFile = folder.resolve("content.yaml")
        val contentFiles = folder.resolve("content").listFiles()!!
        contentFiles.sortBy { it.nameWithoutExtension.toInt() }
        val contents = mutableMapOf<String, Dialogue>()
        for (file in contentFiles) {
            println(file.nameWithoutExtension)
            val readText = file.readText()
            val doc = Jsoup.parse(readText)
            val title = doc.select("h2").first()!!.text()
            val type = title.take(title.indexOf(" ("))

            val tables = doc.select("table")
            val nextRows = tables.first()!!.select("a").map {
                it.attr("href").removeSuffix(".html").toIntOrNull() ?: -1
            }
            val nextCountRows = tables.first()!!.select("a").map {
                it.text().toInt()
            }

            val previousRows = tables.last()!!.select("a").map {
                it.attr("href").removeSuffix(".html").toIntOrNull() ?: -1
            }

            val previousCountRows = tables.last()!!.select("a").map {
                it.text().toInt()
            }

            when (type) {
                "DIALOGUE_OPTIONS" -> {
                    val list = doc.select("ol").first()!!.select("li")
                    val options = list.mapNotNull { if (it.hasText()) it.text() else null }
                    val id = file.nameWithoutExtension
                    contents[id] = Dialogue(id.toInt(), type, nextRows, nextCountRows, previousRows, previousCountRows, options)
                }
                "DIALOGUE_PLAYER", "DIALOGUE_NPC" -> {
                    val text = doc.select("p").first()!!
                    val name = text.select("b").text()
                    val dialogue = text.text().removePrefix("$name:").trim()
                    val id = file.nameWithoutExtension
                    contents[id] = Dialogue(id.toInt(), type, nextRows, nextCountRows, previousRows, previousCountRows, text = dialogue, name = name)
                }
                else -> println("Unknown type $type")
            }
        }
        println("Done")

        val yaml = Yaml()
        val config = object : YamlWriterConfiguration(forceQuoteStrings = true, formatExplicitListSizeLimit = Int.MAX_VALUE) {
            override fun write(value: Any?, indent: Int, parentMap: String?): Any? = super.write(if (value is Dialogue) value.toMap() else value, indent, parentMap)
        }
        yamlFile.writeText(yaml.writeToString(contents, config))
    }

    private fun npcs(folder: File) {
        val yamlFile = folder.resolve("npcs.yaml")
        val files = folder.resolve("npcs").listFiles()!!
        val npcs = mutableMapOf<String, List<Int>>()
        for (file in files) {
            npcs[file.nameWithoutExtension] = Jsoup.parse(file.readText()).select("a").map { it.attr("href").removePrefix("../content/").removeSuffix(".html").toInt() }
        }

        val yaml = Yaml()
        val config = YamlWriterConfiguration(forceQuoteStrings = true, forceQuoteKeys = true, formatExplicitListSizeLimit = Int.MAX_VALUE)
        yamlFile.writeText(yaml.writeToString(npcs, config))
    }
}

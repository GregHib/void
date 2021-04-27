package world.gregs.voidps.tools.wiki

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.tools.wiki.model.Infobox
import world.gregs.voidps.tools.wiki.model.Wiki
import java.io.File
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter

object MusicInfoBoxDumper {

    @JvmStatic
    fun main(args: Array<String>) {
        val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
        val revision = LocalDate.of(2011, Month.OCTOBER, 16)

        val file = File("music_tracks.yml")
        val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule().writerWithDefaultPrettyPrinter()
        val wiki = Wiki.load("${System.getProperty("user.home")}\\Downloads\\Old+School+RuneScape+Wiki-20210427125152.xml")
        val missing = mutableListOf<String>()
        val output = mutableMapOf<String, Any>()

        wiki.pages.forEach { page ->
            val text = page.revision.text
            if (text.contains("infobox music", true)) {
                val template = Infobox.getFirstList(page, listOf("infobox music"))?.toMap() ?: throw IllegalArgumentException("Uh oh")
                val name = template["name"] as String
                val map = template["map"] as? List<Pair<String, String>>
                var release = (template["release"] as String).replace("[[", "").replace("]]", "").removePrefix("Intended for ")
                if (release.contains(" - ")) {
                    release = release.split(" - ").last()
                } else if (release == "N/A") {
                    println("Unknown date ${page.title} ${template["release"]}")
                    release = ""
                }
                if (release.isNotBlank()) {
                    val date = LocalDate.parse(release, formatter)
                    if (date.isAfter(revision)) {
                        println("OSRS track skipped $name $date")
                        return@forEach
                    }
                }
                if (map != null) {
                    val coords = map.filter { it.first == "" }
                    if (coords.isEmpty()) {
                        missing.add(name)
                    } else {
                        val plane = map.firstOrNull { it.first == "plane" }?.second?.toInt() ?: 0
                        output[name] = coords.map {
                            val parts = it.second.split(",")
                            Tile(parts[0].toInt(), parts[1].toInt(), plane)
                        }
                    }
                }
            }
        }

        println(missing)
//        System.exit(0)

        val regex = "\\[([0-9]+),([0-9]+)]".toRegex()
        val temp = File("${System.getProperty("user.home")}\\Downloads\\Old+School+RuneScape+Wiki-20210427143338.xml")
        Wiki.load(temp.path).pages.forEach { page ->
            val text = page.revision.text
            if (text.contains("music track map", true)) {
                val template = Infobox.getFirstList(page, listOf("music track map"))?.toMap() ?: throw IllegalArgumentException("Uh oh")
                val coords = template[""] as? String ?: return@forEach
                val coordinates = mutableListOf<Tile>()
                for (coord in regex.findAll(coords)) {
                    val x = coord.groupValues[1].toInt()
                    val y = coord.groupValues[2].toInt()
                    coordinates.add(Tile(x, y))
                }
                output[page.title.removePrefix("Map:")] = coordinates
            }
        }

        mapper.writeValue(file, output)
    }

}
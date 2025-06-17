package world.gregs.voidps.tools.wiki

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.decoder.EnumDecoder
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.DefinitionsDecoder.Companion.toIdentifier
import world.gregs.voidps.tools.wiki.model.Infobox
import world.gregs.voidps.tools.wiki.model.Wiki
import world.gregs.voidps.type.Tile
import java.io.File
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter

@Suppress("UNCHECKED_CAST")
object MusicInfoBoxDumper {

    @JvmStatic
    fun main(args: Array<String>) {
        Settings.load()
        val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
        val revision = LocalDate.of(2011, Month.OCTOBER, 16)

        val file = File("music_tracks.yml")
        val mapper = ObjectMapper(
            YAMLFactory().apply {
                disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
                enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
                disable(YAMLGenerator.Feature.SPLIT_LINES)
            },
        ).registerKotlinModule()
        // Category:Music_tracks Dump
        val wiki = Wiki.load("${System.getProperty("user.home")}\\Downloads\\Old+School+RuneScape+Wiki-20210427125152.xml")
        val missing = mutableListOf<String>()
        val output = mutableMapOf<String, Any>()

        val cache = CacheDelegate(Settings["storage.cache.path"])
        val defs = EnumDecoder().load(cache)
        val enum = defs[1345]
        val enumMap = enum.map!!.mapValues { (_, value) -> toIdentifier(value as String) }
        println(enumMap)

        wiki.pages.forEach { page ->
            val text = page.revision.text
            if (text.contains("infobox music", true)) {
                val template = Infobox.getFirstList(page, listOf("infobox music"))?.toMap() ?: throw IllegalArgumentException("Uh oh")
                val name = template["name"] as String
                val map = template["map"] as? List<List<Pair<String, String>>> ?: emptyList()
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
                        return@forEach
                    }
                }
                val nme = toIdentifier(
                    name
                        .replace("&", "and")
                        .replace(" 1", "1")
                        .replace(" 2", "2")
                        .replace(" 3", "3")
                        .replace(" 4", "4")
                        .replace(" 5", "5")
                        .replace(" 6", "6")
                        .replace(" 6", "6"),
                )
                    .replace("brew_hoo_hoo!", "brew_hoo_hoo")
                    .replace("davy_jones_locker", "davy_joness_locker")
                    .replace("chef_surprise", "chef_surprize")
                    .replace("scape_home", "homescape")
                    .replace("scape_ground", "ground_scape")
                    .replace("h.a.m._and_seek", "ham_and_seek")
                    .replace("h.a.m._attack", "ham_attack")
                    .replace("the_quizmaster", "the_quiz_master")
                    .replace("spirits_of_the_elid", "spirits_of_elid")
                    .replace("eagles_peak", "eagle_peak")
                    .replace("autumn_in_bridgelum", "eagle_peak")
                    .replace("wolf_mountain", "wild_isle")
                    .replace("the_maze", "melzars_maze")
                    .replace("etceteria", "etcetera")
                // 987=darkmeyer
                val idx = enumMap.values.indexOf(nme)
                if (idx == -1) {
                    println("No match for $nme")
                }
                val index = if (idx == -1) -1 else enumMap.keys.toList()[idx]
                val areas = mutableListOf<Map<String, Any>>()
                for (m in map) {
                    val coords = m.filter { it.first == "" }
                    if (coords.isEmpty()) {
                        missing.add(name)
                    } else {
                        val level = m.firstOrNull { it.first == "level" }?.second?.toInt() ?: 0
                        areas.add(
                            mapOf(
                                "x" to "[ ${coords.map { it.second.split(",")[0].toInt() }.joinToString(", ")} ]",
                                "y" to "[ ${coords.map { it.second.split(",")[1].toInt() }.joinToString(", ")} ]",
                                "level" to level,
                            ),
                        )
                    }
                }
                if (areas.isNotEmpty()) {
                    output[toIdentifier(name)] = mapOf(
                        "index" to index,
//                        "name" to name,
                        "areas" to areas,
                    )
                }
            }
        }

        println("Missing:")
        for (miss in missing) {
            println(miss)
        }
//        System.exit(0)

        val regex = "\\[([0-9]+),([0-9]+)]".toRegex()
        // Dump for missing map pages
        val temp = File("${System.getProperty("user.home")}\\Downloads\\Old+School+RuneScape+Wiki-20210428004138.xml")
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
                val name = page.title.removePrefix("Map:")
                val index = enum.getKey(name)
                output[toIdentifier(name)] = mapOf(
                    "index" to index,
//                    "name" to name,
                    "areas" to listOf(
                        mapOf(
                            "x" to "[ ${coordinates.map { it.x }.joinToString(", ")} ]",
                            "y" to "[ ${coordinates.map { it.y }.joinToString(", ")} ]",
                            "level" to coordinates.first().level,
                        ),
                    ),
                )
            }
        }

        mapper.writeValue(file, output.toList().sortedBy { (it.second as Map<String, Any>)["index"] as Int }.toMap())
    }
}

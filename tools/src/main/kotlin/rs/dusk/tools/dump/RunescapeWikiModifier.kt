package rs.dusk.tools.dump

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.koin.core.context.startKoin
import rs.dusk.cache.definition.decoder.ItemDecoder
import rs.dusk.engine.client.cacheDefinitionModule
import rs.dusk.engine.client.cacheModule
import java.io.File
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter

/**
 * Takes raw [RunescapeWikiDumper] data converts to usable data for dusk
 */
internal object RunescapeWikiModifier {

    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule)
        }.koin
        val decoder = ItemDecoder(koin.get())

        val file = File("./ItemsPretty.json")
        val text = file.readText()
        val mapper = ObjectMapper(JsonFactory())
        val raw: MutableMap<String, MutableMap<String, String>> = mapper.readValue(text)
        raw["Antique lamp (Gunnar's Ground)"]!!["Item ID"] = "19775"
        raw["Antique lamp (Medium Karamja Tasks)"]!!["Item ID"] = "11139"

        val output = mutableMapOf<Int, MutableMap<String, String>>()
        for ((item, properties) in raw) {
            val ids = properties.getIds()
            for(id in ids) {
                output[id] = properties
            }

        }

        for ((item, properties) in raw) {
            val ids = properties.getIds()
            if(ids.isEmpty() && releasedBefore(properties, revision)) {
                val name = properties["Name"] ?: ""
                val found = mutableListOf<Int>()
                repeat(decoder.size) { id ->
                    if(output.containsKey(id)) {
                        return@repeat
                    }
                    val def = decoder.get(id)
                    if(name == def.name) {
                        found.add(id)
                    }
                }
                if((item.contains("Rod of ivandis") || item.contains("(easy)") || item.contains("(medium)") || item.contains("(hard)") || item.contains("(elite)")) && found.isNotEmpty()) {
                    found.forEach {
                        output[it] = properties
                    }
                    println("Found $item $found")
                } else {
                    println("Unhandled item $item")
                }
            }
        }

        output.forEach { (_, properties) ->
            properties.trimWeight()
            properties.remove("Value")
            properties.remove("Equipable")
            properties.remove("Stackable")
            properties.remove("Disassembly")
            properties.remove("Exchange")
            properties.remove("Release")
            properties.remove("Quest item")
            properties.trimCoins("High alch")
            properties.trimCoins("Low alch")
        }
        save(output.toSortedMap(), "Items667")
    }

    fun MutableMap<String, String>.trimWeight() {
        val weight = this["Weight"] ?: "?"
        if(weight != "?") {
            this["Weight"] = weight.removeSuffix(" kg")
        }
    }

    fun MutableMap<String, String>.trimCoins(key: String) {
        val weight = this[key] ?: "?"
        if(weight != "?") {
            this[key] = weight.removeSuffix(" coins").removeSuffix(" coin")
        }
    }

    fun releasedBefore(map: MutableMap<String, String>, revision: LocalDate): Boolean {
        val date = map.getRelease()
        if(date != null && date.isBefore(revision)) {
            return true
        }
        return false
    }

    fun isRevision(map: MutableMap<String, String>): Boolean {
        val ids = map.getIds()
        for (itemId in ids) {
            if (itemId <= maxItemId) {
                return true
            }
        }
        return false
    }

    val maxItemId = 22323
    val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
    val revision = LocalDate.of(2011, Month.OCTOBER, 4)

    fun Map<String, String>.getRelease(): LocalDate? {
        val release = this["Release"] ?: "?"
        if (release == "?") {
            return null
        }
        return LocalDate.parse(release, formatter)
    }

    var spaceCount = 0
    var commaCount = 0
    fun Map<String, String>.getIds(): List<Int> {
        val list = mutableListOf<Int>()
        val property = this["Item ID"] ?: "?"
        if (property != "?") {
            if (property.contains(", ")) {
                spaceCount++
                property.split(", ").forEach {
                    val id = it.toIntOrNull()
                    if (id != null) {
                        list.add(id)
                    }
                }
            } else if (property.contains(",")) {
                commaCount++
                property.split(",").forEach {
                    val id = it.toIntOrNull()
                    if (id != null) {
                        list.add(id)
                    }
                }
            } else {
                val id = property.toIntOrNull()
                if (id != null) {
                    list.add(id)
                } else {
                    println("Unable to process id $property")
                }
            }
        }
        return list
    }

    fun formatBoolean(map: MutableMap<String, Any>, key: String) {
        val value = map[key] as? String ?: return
        val bool = when (value) {
            "Yes" -> true
            "No" -> false
            else -> {
                println("Invalid boolean $key $value")
                return
            }
        }
        map[key] = bool
    }

    fun save(map: Map<Int, Any>, name: String) {
        val writer = ObjectMapper(JsonFactory()).writerWithDefaultPrettyPrinter()
        val out = File("./$name.json")
        writer.writeValue(out, map)
    }
}
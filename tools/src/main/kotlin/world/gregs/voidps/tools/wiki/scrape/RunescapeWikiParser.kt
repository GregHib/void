package world.gregs.voidps.tools.wiki.scrape

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File

/**
 * Takes raw [RunescapeWikiScraper] data and removes formatting
 */
internal object RunescapeWikiParser {

    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun main(args: Array<String>) {
        val file = File("./data/dump/Items.json")
        val text = file.readText()
        val mapper = ObjectMapper(JsonFactory())
        val raw: MutableMap<String, Any> = mapper.readValue(text)
        raw.remove("")
        val output = mutableMapOf<String, Any>()
        var total = 0
        for ((letter, map) in raw) {
            val letters = map as MutableMap<String, MutableMap<String, Any>>
            letters.forEach { (item, data) ->
                if (data.keys.isEmpty()) {
                    return@forEach
                }
                replaceExamines(data)
                removeAdvancedData(data)
                removeGrandExchangeData(data)
                // Sort by approx name first
                val keys = data.keys.toList().sortedBy { it.length - item.length }
                val name = data.keys.first()
                keys.forEach { key ->
                    flatten(data, key)
                }
                data["Name"] = name

                formatReleaseDate(data)
                formatExchangePrice(data)
                data.forEach { (key, _) ->
                    formatEdit(data, key)
                }
                output[item] = data
            }

            total += letters.size
        }

        savePretty(output)
        saveNormal(output)
    }

    @Suppress("UNCHECKED_CAST")
    fun replaceExamines(map: MutableMap<String, Any>) {
        val property = map["Examine"] as? MutableMap<String, String> ?: return
        property["Examine"] = property.remove("") ?: return
    }

    @Suppress("UNCHECKED_CAST")
    fun removeAdvancedData(map: MutableMap<String, Any>) {
        val properties = map["Advanced data"] as? MutableMap<String, String> ?: return
        properties.remove("Links")
        properties.remove("")
    }

    @Suppress("UNCHECKED_CAST")
    fun removeGrandExchangeData(map: MutableMap<String, Any>) {
        val properties = map["Grand Exchange"] as? MutableMap<String, String> ?: return
        properties.remove("")
    }

    fun formatReleaseDate(map: MutableMap<String, Any>) {
        val value = map["Release"] as? String ?: return
        map["Release"] = value.removeSuffix(" (Update)")
    }

    fun formatExchangePrice(map: MutableMap<String, Any>) {
        val value = map["Exchange"] as? String ?: return
        map["Exchange"] = value.removeSuffix(" (info)")
    }

    fun formatEdit(map: MutableMap<String, Any>, key: String) {
        val value = map[key] as? String ?: return
        map[key] = value.removeSuffix(" (edit)")
    }

    @Suppress("UNCHECKED_CAST")
    fun flatten(map: MutableMap<String, Any>, name: String) {
        val properties = map.remove(name) as? MutableMap<String, String> ?: return
        properties.forEach { (key, value) ->
            if (map.containsKey(key)) {
                println("Overridden key $key")
            }
            map[key] = value
        }
    }

    fun savePretty(map: Map<String, Any>) {
        val writer = ObjectMapper(JsonFactory()).writerWithDefaultPrettyPrinter()
        val out = File("./data/dump/ItemsPretty.json")
        writer.writeValue(out, map)
    }

    fun saveNormal(map: Map<String, Any>) {
        val writer = ObjectMapper(JsonFactory())
        val out = File("./data/dump/ItemsNormal.json")
        writer.writeValue(out, map)
    }
}

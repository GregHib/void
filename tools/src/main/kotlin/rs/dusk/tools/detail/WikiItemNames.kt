package rs.dusk.tools.detail

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.koin.core.context.startKoin
import rs.dusk.cache.Cache
import rs.dusk.cache.CacheDelegate
import rs.dusk.cache.definition.decoder.ItemDecoder
import rs.dusk.engine.client.cacheDefinitionModule
import rs.dusk.engine.client.cacheModule
import rs.dusk.engine.data.file.FileLoader
import rs.dusk.engine.data.file.fileLoaderModule
import rs.dusk.engine.entity.item.EquipSlot
import rs.dusk.engine.entity.item.EquipType
import rs.dusk.tools.convert.ItemDecoder718
import java.io.File

/**
 * Dumps unique string identifiers for items using formatted item definition name plus index for duplicates
 */
private class WikiItemNames(val decoder: ItemDecoder, val types: ItemTypes, val raw: MutableMap<Int, MutableMap<String, Any>>) : NameDumper() {

    val map = raw.map { (it.value["String ID"] as? String ?: it.value["Name"] as String).toLowerCase() to it.key }.toMap()

    val data = mutableMapOf<Int, Map<String, Any>>()

    override fun createName(id: Int): String? {
        val decoder = decoder.getOrNull(id) ?: return "null"
        val name = decoder.name

        if(name.toLowerCase() == "null") {
            return "null"
        }

        var out = when {
            !raw.containsKey(id) && !map.containsKey(name.toLowerCase()) && name != "null" -> {
                val result = map[name.toLowerCase()] ?: map[suffixes(name, false).toLowerCase()] ?: map[suffixes(name, true).toLowerCase()]
                if (result != null) {
                    val rawData = raw[result]
                    if (rawData == null) {
                        name
                    } else {
                        data[id] = rawData
                        chooseAString(rawData["String ID"] as? String, name, id)
                    }
                } else {
                    name
                }
            }
            raw.containsKey(id) -> {
                val rawData = raw[id]
                if (rawData == null) {
                    name
                } else {
                    data[id] = rawData
                    chooseAString(rawData["String ID"] as? String, name, id)
                }
            }
            map.containsKey(name.toLowerCase()) -> {
                val mapId = map[name.toLowerCase()]
                if (mapId == null) {
                    name
                } else {
                    val rawData = raw[mapId]
                    if (rawData == null) {
                        name
                    } else {
                        data[id] = rawData
                        chooseAString(rawData["String ID"] as? String, name, id)
                    }
                }
            }
            else -> {
                decoder.name
            }
        }
        out = when {
            decoder.notedTemplateId != -1 -> "${out}_noted"
            decoder.lendTemplateId != -1 -> "${out}_lent"
            decoder.singleNoteTemplateId != -1 -> "${out}_note"
            else -> out
        }
        return out
    }

    fun chooseAString(id: String?, name: String, item: Int): String {
        if (id == null) {
            return name
        }
        if (name.contains("broodoo", true)) {
            val colour = when (item) {
                in 6215..6236 -> "green"
                in 6237..6258 -> "orange"
                in 6259..6280 -> "blue"
                else -> ""
            }
            return "$name ($colour)"
        }
        if (name.contains("statius'", true)) {
            return name
        }
        for(i in 0..100) {
            if(name.endsWith(" $i")) {
                return name
            }
        }
        if (name.contains("(") || name.contains(")") || name.contains("/") || name.startsWith("Worn-out", true)) {
            return name
        }

        if (renames.keys.any { name.contains(it, true) }) {
            return name
        }

        return id
    }

    fun suffixes(string: String, s: Boolean): String {
        var value = string
        if(value.contains("broodoo", true)) {
            return "Broodoo shield (blue)"
        }
        preffixes.forEach {
            value = value.removePrefix(it)
        }
        for (i in 0..100) {
            value = value.removeSuffix(" ($i)")
            value = value.removeSuffix(" $i")
        }
        suffixes.forEach {
            value = value.removeSuffix(it)
        }
        if (s) {
            value = value.removeSuffix("s")
        }
        if (value.contains("statius'", true) && !value.contains("statius's", true)) {
            value = value.replace("statius'", "statius's", true)
        }
        if (value.contains("cake", true) && !value.contains("chocolate", true)) {
            value = value.replace("cake", "slice of cake", true)
        }
        renames.forEach { (old, new) ->
            value = value.replace(old, new, true)
        }
        return value
    }

    val preffixes = setOf(
            "Worn-out ",
            "1/2 ",
            "2/3 "
    )
    val suffixes = setOf(
            " full",
            " 9/10",
            " 8/10",
            " 7/10",
            " 6/10",
            " 5/10",
            " 4/10",
            " 3/10",
            " 2/10",
            " 1/10",
            " (b)",
            " (p)",
            " (p+)",
            " (p++)",
            " (kp)",
            " (deg)",
            " (broken)"
    )

    val renames = mapOf(
            "chocolate cake" to "Chocolate slice",
            "p'apple" to "Pineapple",
            "Half a meat pie" to "Meat pie",
            "Half a redberry pie" to "Redberry pie",
            "Half an apple pie" to "Apple pie",
            "Dwarven battleaxe" to "Dwarven battleaxe (sharp)",
            "Bronze jav'n" to "Bronze javelin",
            "Pot of tea" to "Pot of tea (white)",
            "Adam platebody" to "Adamant platebody",
            "Forinthry brace" to "Forinthry bracelet",
            "Digsite pendant" to "Dig Site pendant",
            "Void " to "Void knight ",
            "d'hide" to "dragonhide",
            "C. " to "Corrupt ",
            "dragon med helm" to "dragon helm",
            "(level 2) " to "",
            "(level 3) " to "",
            "(level 4) " to "",
            "(level 5) " to "",
            "Kayle's sling" to "Kayle's chargebow",
            "Recover special" to "Adrenaline potion",
            "arrowtips" to "arrowheads",
            "thrownaxe" to "throwing axe",
            "longbow" to "shieldbow",
            "signed oak bow" to "signed oak shieldbow",
            "h'ween" to "halloween"
    )

    override fun createData(name: String, id: Int): Map<String, Any> {
        val map = LinkedHashMap<String, Any>()
        map["id"] = id
        val slot = types.slots[id]
        if (slot != null) {
            var s = EquipSlot.by(slot)
            if (id == 11277) {
                s = EquipSlot.Hat
            }
            if (s == EquipSlot.None) {
                println("Unknown slot $slot $id")
            } else {
                map["slot"] = s.name
            }
        }
        val type = types.getEquipType(id)
        if (type != EquipType.None) {
            map["type"] = type.name
        }
        data[id]?.let { rawData ->
            val data = rawData.mapKeys { it.key.toLowerCase() }
            order.forEach { key ->
                val value = data[key] ?: return@forEach
                if(name.endsWith("_noted") && key != "id" && key != "alchable") {
                    return@forEach
                }
                if(key == "examine") {
                       var replaced = false
                       for (i in 2..4) {
                           if (name.contains("($i)")) {
                               map[key.toLowerCase()] = (value as String).replace("1 dose of", "$i doses of")
                               replaced = true
                           }
                       }
                       if (!replaced) {
                           map[key.toLowerCase()] = value
                       }
                } else {
                    map[key.toLowerCase()] = value
                }
            }
        }
        return map
    }

    override fun sortList(key: String, list: MutableList<Int>): MutableList<Int> {
        return list.sortedBy { if(raw.containsKey(it)) it else Int.MAX_VALUE }.toMutableList()
    }

    companion object {

        val order = listOf(
                "id",
                "slot",
                "type",
                "weight",
                "edible",
                "tradeable",
                "alchable",
                "lendable",
                "bankable",
                "individual",
                "limit",
                "demise",
                "destroy",
                "examine"
        )

        private fun loadEquipSlotsAndTypes() {
            val decoder718 = ItemDecoder718(CacheDelegate("${System.getProperty("user.home")}\\Downloads\\rs718_cache\\", "1", "1") as Cache)
            repeat(decoder718.size) { id ->
                decoder718.get(id)
            }
        }

        @JvmStatic
        fun main(args: Array<String>) {
            val koin = startKoin {
                fileProperties("/tool.properties")
                modules(cacheModule, cacheDefinitionModule, fileLoaderModule)
            }.koin
            loadEquipSlotsAndTypes()
            val decoder = ItemDecoder(koin.get())
            val loader = FileLoader(true)
            val types = ItemTypes(decoder)

            val text = File("./data/dump/Items667.json").readText()
            val mapper = ObjectMapper(JsonFactory())
            val raw: MutableMap<Int, MutableMap<String, Any>> = mapper.readValue(text)

            val names = WikiItemNames(decoder, types, raw)
            names.dump(loader, "./data/dump/item-details.yml", "item", decoder.size)
        }
    }

}
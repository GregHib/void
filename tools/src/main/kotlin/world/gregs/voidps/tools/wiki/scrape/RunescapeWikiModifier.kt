package world.gregs.voidps.tools.wiki.scrape

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.engine.data.Settings
import java.io.File
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter

/**
 * Takes raw [RunescapeWikiScraper] data converts to usable data for dusk
 */
internal object RunescapeWikiModifier {

    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun main(args: Array<String>) {
        Settings.load()
        val cache: Cache = CacheDelegate(Settings["storage.cache.path"])
        val decoder = ItemDecoder().load(cache)

        val file = File("./data/dump/ItemsPretty.json")
        val text = file.readText()
        val mapper = ObjectMapper(JsonFactory())
        val raw: MutableMap<String, MutableMap<String, String>> = mapper.readValue(text)
        raw["Antique lamp (Gunnar's Ground)"]!!["Item ID"] = "19775"
        raw["Antique lamp (Medium Karamja Tasks)"]!!["Item ID"] = "11139"

        val output = mutableMapOf<Int, MutableMap<String, Any>>()
        for ((item, properties) in raw) {
            val ids = properties.getIds()
            val date = properties.getRelease()
//            if (date == null || date.isBefore(revision)) {
            for (id in ids) {
//                    if (id < maxItemId) {
                val props = HashMap(properties as MutableMap<String, Any>)
                props["Item ID"] = id
                props["String ID"] = item
                output[id] = props
//                    }
            }
//            }
        }

        identifyMissingIds(raw, decoder, output)

        output.forEach { (_, properties) ->
            properties.trimWeight()
            properties.formatInt("Buy limit")
            properties.remove("Value")
            properties.remove("Equipable")
            properties.remove("Stackable")
            properties.remove("Disassembly")
            properties.remove("Exchange")
            properties.remove("Release")
            properties.remove("Quest item")
            properties.remove("Lendable")
            properties.remove("High alch")
            properties.remove("Low alch")
            properties.remove("Noteable")
            properties.remove("Removal")
            properties.remove("Members")
            properties.remove("Buy limit")?.let {
                properties["Limit"] = it
            }
            properties.replaceBool("Stacks in bank", false)
            properties.remove("Stacks in bank")?.let {
                if (it == false) {
                    properties["Individual"] = it
                }
            }
            properties.remove("Weight")?.let {
                if (it as Double > 0.0) {
                    properties["Weight"] = it
                }
            }
            properties.replaceBool("Edible", false)
            properties.replaceBool("Tradeable", true)
            properties.replaceBool("Bankable", true)
            properties.replaceMultilines("Examine")
            properties.replaceMultilines("Destroy")
            properties.updateExamines()
            properties.updateDestroy()
            properties.updateNames("Name")
            properties.updateNames("String ID")
            properties.onDeathTypes()
            properties.dropTypes()
            properties.alch()
        }
        save(output.toSortedMap(), "Items667")
    }

    @Suppress("UNCHECKED_CAST")
    private fun identifyMissingIds(
        raw: MutableMap<String, MutableMap<String, String>>,
        decoder: Array<ItemDefinition>,
        output: MutableMap<Int, MutableMap<String, Any>>,
    ) {
        for ((item, properties) in raw) {
            val ids = properties.getIds()
            if (ids.isEmpty() && releasedBefore(properties, revision)) {
                val name = properties["Name"] ?: ""
                val found = mutableListOf<Int>()
                for (id in decoder.indices) {
                    if (output.containsKey(id)) {
                        continue
                    }
                    val def = decoder[id]
                    if (name == def.name) {
                        found.add(id)
                    }
                }
                if ((item.contains("Rod of ivandis") || item.contains("(easy)") || item.contains("(medium)") || item.contains("(hard)") || item.contains("(elite)")) && found.isNotEmpty()) {
                    found.forEach {
                        val props = HashMap(properties as MutableMap<String, Any>)
                        props["Item ID"] = it
                        output[it] = props
                    }
                    println("Found $item $found")
                } else {
                    println("Unhandled item $item")
                }
            }
        }
    }

    private fun MutableMap<String, Any>.replaceMultilines(key: String) {
        val value = this[key] as? String ?: "?"
        if (value.contains("•")) {
            val parts = value.split("•")
            this[key] = parts.first { it.isNotBlank() }.trim()
        } else if (value.startsWith("*")) {
            val parts = value.split("*")
            this[key] = parts.first { it.isNotBlank() }.trim()
        } else if (value.contains("Item bonus:")) {
            this[key] = value.substring(0, value.indexOf("Item bonus:") - 1)
        } else if (value.contains(":") && !value.startsWith("'") && !exceptions.any { value.contains(it, true) }) {
            this[key] = value.substring(value.lastIndexOf(":") + 1, value.length).trim()
        }
    }

    private val exceptions = setOf("incense", "warning", "size:", "village:", "clairvoyance:", "disclaimer:", "bonus:", "incubator:", "parts:", "says:", "pork:", "danger:")

    private fun MutableMap<String, Any>.replaceBool(key: String, default: Boolean) {
        val value = remove(key) as? String ?: "?"
        if (value != "?") {
            when (value) {
                "Yes", "yes", "Restricted" -> {
                    if (!default) {
                        this[key] = true
                    }
                }
                "No", "no" -> {
                    if (default) {
                        this[key] = false
                    }
                }
                else -> {
                    println("Unknown $key '$value'")
                }
            }
        }
    }

    private fun MutableMap<String, Any>.trimWeight() {
        val weight = this["Weight"] as? String ?: "?"
        if (weight != "?") {
            val kg = weight.removeSuffix(" kg").toDoubleOrNull()
            if (kg == null) {
                println("Unknown weight '$weight'")
                remove("Weight")
            } else {
                this["Weight"] = kg
            }
        } else {
            remove("Weight")
        }
    }

    private fun MutableMap<String, Any>.formatInt(key: String) {
        val value = this[key] as? String ?: "?"
        if (value != "?") {
            val kg = value.replace(",", "").toIntOrNull()
            if (kg == null) {
                println("Unknown $key '$value'")
                remove(key)
            } else {
                this[key] = kg
            }
        } else {
            remove(key)
        }
    }

    private fun MutableMap<String, Any>.updateDestroy() {
        val key = "Destroy"
        var value = this[key] as? String ?: return
        value = value.replace(" (It is immediately removed from your inventory)", "")
        this[key] = value
    }

    private fun MutableMap<String, Any>.updateExamines() {
        val key = "Examine"
        var value = this[key] as? String ?: return
        value = value.replace("improved to be held in the off-hand.", "improved")
        value = value.replace("A right-hand fighting claw.", "A set of fighting claws.")
        value = value.replace(" Melee weapon, requires Attack (1).", "")
        value = value.replace("twin hammer.", "twin hammers.")
        value = value.replace("Your successful attacks fill an additional 0.5% of your adrenaline bar", "Your successful attacks restore 0.2% of your special attack bar.")
        value = value.replace("dose of adrenaline potion.", "dose of Recover special potion.")
        value = value.replace("[sic]", "")
        value = value.replace(regex, "")
        value = value.replace(regex2, "")
        value = value.replace("(The Blood Pact)", "")
        value = value.replace("(The Curse of Arrav)", "")
        value = value.replace("shieldbow; I", "longbow; I")
        value = value.replace("Ammunition for shieldbows", "Ammunition for longbows")
        if (value.contains("(") && !examineExceptions.any { value.contains(it, true) }) {
            value = value.replace(regex3, "")
        }
        this[key] = value.trim()
    }

    private val examineExceptions = setOf(
        "yes",
        "normal",
        "oak",
        "willow",
        "maple",
        "incense",
        "acadia",
        "2x",
        "trimmed",
        "tier",
        "ornamental",
        "edition",
        "minutes",
        "coal",
        "copper and tin",
        "resources",
        "mithril",
        "adamantite",
        "runite",
        "baby",
        "PvP",
        "stackable",
        "upgraded",
        "orichalcite",
        "1h",
        "hopefully",
        "%",
        "zemouregal",
        "additional",
        "superheat",
        "Fungi",
        "Taken from a",
        ",000",
        "yet evil",
        "animica",
        "Place this to gather herbs",
        "fashioned from crystal",
        "Christmas jumper",
        "dudette",
        "and people",
        "decorated",
        "forlorn",
        "stake-thrower",
        "clairvoyance",
        "overheat",
    )
    private val regex = "Used (in|with) (.*)\\([0-9,\\s&]+\\)".toRegex()
    private val regex2 = "Requires (.*)\\([0-9,\\s&]+\\)".toRegex()
    private val regex3 = "\\(.*\\)".toRegex()

    private val gods = setOf("Ancient", "Armadyl", "Bandos", "Gilded", "Guthix", "Saradomin", "Zamorak", "Zaros")

    private fun MutableMap<String, Any>.updateNames(key: String) {
        var value = this[key] as? String ?: return
        if (value == "Dragon claw") {
            value = "Dragon claws"
        }
        value = value.replace("Black claw", "Black claws")
        value = value.replace("White claw", "White claws")
        value = value.replace("Torag's hammer", "Torag's hammers")
        if (value.contains("plate") || value.contains("kiteshield") || value.contains("full helm")) {
            arrayOf("platebody", "platelegs", "plateskirt", "kiteshield", "full helm").forEach { type ->
                gods.forEach { god ->
                    value = if (god == "Zaros") {
                        value.replace("Rune $type ($god)", "Ancient $type")
                    } else {
                        value.replace("Rune $type ($god)", "$god $type")
                    }
                }
            }
        }
        if (value.contains("Blessed dragonhide ")) {
            arrayOf("vambraces", "coif", "chaps", "body").forEach { type ->
                gods.forEach { god ->
                    value = if (type == "body" && (god == "Saradomin" || god == "Zamorak" || god == "Guthix")) {
                        value.replace("Blessed dragonhide $type ($god)", "$god dragonhide")
                    } else {
                        value.replace("Blessed dragonhide $type ($god)", "$god $type")
                    }
                }
            }
        }
        this[key] = value
    }

    private fun MutableMap<String, Any>.dropTypes() {
        val value = this["Destroy"] as? String ?: return
        if (value == "?" || value.startsWith("Drop", true) || value == "Destroy") {
            remove("Destroy")
        }
    }

    private fun MutableMap<String, Any>.alch() {
        val value = remove("Alchemy") as? String ?: return
        if (value == "Not alchemisable") {
            this["Alchable"] = false
        }
    }

    private fun MutableMap<String, Any>.onDeathTypes() {
        // Default is "Drop"
        val value = remove("On death") as? String ?: return
        if (value.contains("Reclaimable")) {
            val tradeable = this["Tradeable"] as? Boolean ?: true
            if (!tradeable) {
                this["Demise"] = "Reclaim"
            }
        } else {
            if (value != "Dropped on death") {
                this["Demise"] = when (value) {
                    "Always kept outside Wild" -> "Wilderness"
                    "Always a safe death" -> "Priority"
                    "Always kept" -> "Always"
                    else -> "Vanish"
                }
            }
        }
    }

    private fun releasedBefore(map: MutableMap<String, String>, revision: LocalDate): Boolean {
        val date = map.getRelease()
        return date != null && date.isBefore(revision)
    }

    const val MAX_ITEM_ID = 22323
    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
    val revision: LocalDate = LocalDate.of(2011, Month.OCTOBER, 4)

    private fun Map<String, String>.getRelease(): LocalDate? {
        val release = this["Release"] ?: "?"
        if (release == "?") {
            return null
        }
        return LocalDate.parse(release, formatter)
    }

    private var spaceCount = 0
    private var commaCount = 0

    private fun Map<String, String>.getIds(): List<Int> {
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

    fun save(map: Map<Int, Any>, name: String) {
        val writer = ObjectMapper(JsonFactory()).writerWithDefaultPrettyPrinter()
        val out = File("./data/dump/$name.json")
        writer.writeValue(out, map)
    }
}

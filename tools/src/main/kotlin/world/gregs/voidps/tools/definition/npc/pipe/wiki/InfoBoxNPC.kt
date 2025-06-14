package world.gregs.voidps.tools.definition.npc.pipe.wiki

import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.engine.data.definition.DefinitionsDecoder.Companion.toIdentifier
import world.gregs.voidps.engine.entity.item.ItemUse
import world.gregs.voidps.tools.Pipeline
import world.gregs.voidps.tools.definition.item.Extras
import world.gregs.voidps.tools.definition.item.pipe.extra.wiki.InfoBoxItem
import world.gregs.voidps.tools.definition.item.pipe.extra.wiki.InfoBoxItem.Companion.formatLineBreaks
import world.gregs.voidps.tools.definition.item.pipe.extra.wiki.InfoBoxItem.Companion.removeLinks
import world.gregs.voidps.tools.definition.item.pipe.extra.wiki.InfoBoxItem.Companion.removeParentheses
import world.gregs.voidps.tools.definition.item.pipe.extra.wiki.InfoBoxItem.Companion.splitExamine
import world.gregs.voidps.tools.wiki.model.Infobox
import world.gregs.voidps.tools.wiki.model.Infobox.forEachVersion
import world.gregs.voidps.tools.wiki.model.Infobox.indexSuffix
import world.gregs.voidps.tools.wiki.model.Infobox.splitByVersion
import world.gregs.voidps.tools.wiki.model.WikiPage
import java.time.LocalDate

class InfoBoxNPC(val revision: LocalDate, private val infoboxes: List<String>) : Pipeline.Modifier<Extras> {
    override fun modify(content: Extras): Extras {
        val (builder, extras) = content
        val (id, name, rs2, _, rs3, rs3Idd, osrs, _) = builder
        processOsrs(extras, osrs)
        processRs3(extras, id, rs3)
        processRs2(extras, rs2)
        builder.uid = if (rs3 != null && rs3Idd) {
            toIdentifier(rs3.title.replace("(historical)", "").trim())
        } else {
            toIdentifier(name)
        }
        return content
    }

    private fun processRs3(extras: MutableMap<String, Any>, id: Int, page: WikiPage?) {
        splitByVersion(page, infoboxes, id, true) { template, suffix ->
            template.forEach { (key, value) ->
                when (val key = key.lowercase()) {
                    "restriction$suffix" -> {
                        val text = (value as String).replace("dg", "dungeoneering")
                        val use = if (text == "removed") {
                            val removal = template["removal"] as? String
                            if (removal.isNullOrBlank() || LocalDate.parse(removeLinks(removal), InfoBoxItem.formatter).isBefore(revision)) {
                                ItemUse.Removed
                            } else {
                                ItemUse.Surface
                            }
                        } else if (text.isNotBlank()) {
                            ItemUse.valueOf(text.lowercase().toSentenceCase())
                        } else {
                            null
                        }
                        if (use != null) {
                            extras.putIfAbsent("area", use)
                        }
                    }
                    "race" -> extras.putIfAbsent(key, removeLinks(value as String))
                    "immune_to_stun$suffix", "immune_to_deflect$suffix", "immune_to_drain$suffix" -> {
                        appendBool(extras, key.removeSuffix(suffix).replace("_to", ""), value as String)
                    }
                    "style$suffix", "primarystyle$suffix", "slayercat$suffix", "assigned_by$suffix" -> {
                        var index = 1
                        (value as String).split(",").forEach lines@{
                            var line = removeLinks(it).lowercase().trim()
                            if (line.isNotBlank()) {
                                if (line == "achtryn") {
                                    line = "mazchna"
                                } else if (line == "morvran" || line == "mandrith" || line == "laniakea") {
                                    return@lines
                                }
                                extras.putIfAbsent(
                                    indexSuffix(
                                        when (key.removeSuffix(suffix)) {
                                            "slayercat" -> "category"
                                            "assigned_by" -> "master"
                                            else -> key.removeSuffix(suffix)
                                        },
                                        index++,
                                    ),
                                    line,
                                )
                            }
                        }
                    }
                    "examine$suffix" -> {
                        splitExamine(value as String, extras, key.removeSuffix(suffix), "", false)
                    }
                }
            }
        }
    }

    private fun processOsrs(extras: MutableMap<String, Any>, page: WikiPage?) {
        forEachVersion(page, infoboxes) { template, suffix ->
            template.forEach { (k, value) ->
                val key = (if (suffix == "1") k.removeSuffix(suffix) else k).lowercase()
                when (k) {
                    "aka", "attributes$suffix" -> extras.putIfAbsent(key, value as String)
                    "members", "aggressive$suffix", "immunepoison$suffix" -> {
                        val text = removeParentheses(value as String)
                        extras.putIfAbsent(if (key == "immunepoison") key.replace("ep", "e_p") else key, text.startsWith("yes", true) || text.equals("immune", true))
                    }
                    "poisonous$suffix" -> {
                        val text = removeLinks(removeParentheses(value as String))
                        val damage = text.toIntOrNull()
                        when {
                            damage != null -> extras.putIfAbsent("poison", damage * 10)
                            text.startsWith("yes", true) -> extras.putIfAbsent("poison", 60)
                            text.startsWith("no", true) || text.isBlank() -> extras.putIfAbsent("poison", 0)
                            text.equals("disease", true) -> extras.putIfAbsent("diseased", true)
                        }
                    }
                    "xpbonus$suffix", "slayxp$suffix" -> {
                        val text = value as String
                        appendDouble(extras, key, text.replace("%", ""))
                    }
                    "max hit$suffix" -> {
                        val text = value as String
                        var index = 1
                        text.replace("+", "").replace(" x 2", "").replace("x2", "").replace("×2", "")
                            .split(",").forEach {
                                val line = removeParentheses(it).trim()
                                if (appendInt(extras, indexSuffix("max", index), line)) {
                                    index++
                                }
                            }
                    }
                    "attack style$suffix", "cat$suffix" -> {
                        val text = value as String
                        val array = text
                            .split(",").map {
                                removeLinks(removeParentheses(it)).trim()
                            }.filter { it.isNotBlank() }
                        if (array.isNotEmpty()) {
                            val key = when (key) {
                                "cat$suffix" -> "category${if (suffix == "1") "" else suffix}"
                                "attack style$suffix" -> "style${if (suffix == "1") "" else suffix}"
                                else -> key
                            }
                            extras.putIfAbsent(key, array.joinToString(separator = ",").lowercase())
                        }
                    }
                    "examine" -> {
                        val text = value as String
                        splitExamine(text, extras, key, "", false)
                    }
                    "race" -> extras.putIfAbsent(key, removeLinks(value as String))
                    "attack speed$suffix" -> {
                        val text = value as String
                        appendInt(extras, "speed", text)
                    }
                    "combat$suffix", "slaylvl$suffix", "hitpoints$suffix", "att$suffix", "str$suffix", "def$suffix", "mage$suffix", "range$suffix", "attbns$suffix", "strbns$suffix", "amagic$suffix", "mbns$suffix", "arange$suffix", "rngbns$suffix", "dstab$suffix", "dslash$suffix", "dcrush$suffix", "dmagic$suffix", "drange$suffix" -> {
                        val text = value as String
                        appendInt(extras, key, text)
                        if (key == "hitpoints") {
                            extras[key.removeSuffix(suffix)] = (extras[key] as? Int ?: return@forEach) * 10
                        }
                    }
                }
            }
        }
    }

    private fun appendInt(extras: MutableMap<String, Any>, key: String, text: String): Boolean {
        val hit = text.toIntOrNull()
        if (hit != null) {
            extras.putIfAbsent(key, hit)
            return true
        } else if (text.isNotBlank() && !text.contains("varies", true) && !text.contains("n/a", true)) {
            println("Unknown $key '$text'")
        }
        return false
    }

    private fun appendBool(extras: MutableMap<String, Any>, key: String, text: String): Boolean {
        if (text.equals("yes", true) || text.equals("no", true)) {
            extras.putIfAbsent(key, text.equals("yes", true))
            return true
        } else if (text.isNotBlank()) {
            println("Unknown $key '$text'")
        }
        return false
    }

    private fun appendDouble(extras: MutableMap<String, Any>, key: String, text: String): Boolean {
        val hit = text.toDoubleOrNull()
        if (hit != null) {
            extras.putIfAbsent(key, hit)
            return true
        } else if (text.isNotBlank()) {
            println("Unknown $key '$text'")
        }
        return false
    }

    private val levelRegex = "Level\\s.*?:\\s?([0-9]+)".toRegex()
    private val levelRegex2 = "([0-9]+)\\s?\\(level\\s[0-9]+\\)".toRegex()
    private val levelRegex3 = "level\\s?([0-9]+)".toRegex()
    private val levelRegex4 = "level[0-9]+\\s([0-9]+)".toRegex()
    private val letters = "[a-zA-Z?<>.:;']".toRegex()

    private fun processRs2(extras: MutableMap<String, Any>, page: WikiPage?) {
        val template = Infobox.getFirstMap(page, infoboxes) ?: return
        template.forEach { (key, value) ->
            when (val key = key.lowercase()) {
                "attack speed" -> {
                    val text = value as String
                    val id = text.toIntOrNull()
                    if (id != null) {
                        extras.putIfAbsent("speed", id)
                    } else {
                        val speed = when (text.lowercase()) {
                            "normal", "same as the normal punch attack speed" -> 4
                            "medium", "with axe is same as 2h, and with spear same as scimitar" -> 7
                            "2 seconds" -> 4
                            "fast" -> 3
                            "longsword" -> 5
                            "speed4.gif", "File:Speed4.gif" -> 4
                            "", "''", "varies", "varies 3", "unknown", "not shown", "---", "1/2" -> -1
                            else -> -2
                        }
                        if (speed > 0) {
                            extras.putIfAbsent("speed", speed)
                        } else if (speed == -2) {
                            println("Unknown speed $value")
                        }
                    }
                }
                "level", "lp" -> {
                    val key = if (key == "lp") "hitpoints" else key
                    val text = removeLinks(value as String)
                    val list = splitLines(text)
                    var index = 1
                    for (it in list.map { removeParentheses(it.trim()) }
                        .flatMap { it.split(" ") }
                        .map { it.replace(letters, "") }
                        .filter { it.isNotBlank() }) {
                        val key = indexSuffix(key, index)
                        if (extras.containsKey(key)) {
                            break
                        }
                        if (appendRange(extras, it, key)) {
                            index++
                        }
                    }
                }
                "immune to poison" -> extras.putIfAbsent("immune", (value as String).equals("yes", true))
                "members", "aggressive", "poisonous" -> {
                    extras.putIfAbsent(key, (value as String).equals("yes", true))
                }
                "weakness" -> {
                    val text = removeLinks(value as String).lowercase().replace(",", "<br>").replace("/", "<br>")
                    var index = 1
                    for (weakness in text.split("<br>")) {
                        if (weakness.isBlank()) {
                            continue
                        }
                        extras.putIfAbsent(indexSuffix(key, index++), weakness.trim())
                    }
                }
                "attack style" -> {
                    val style = (value as String).lowercase()
                    extras.putIfAbsent("style", removeLinks(style))
                }
                "race" -> extras.putIfAbsent(key, removeLinks(value as String))
                "examine" -> {
                    val text = removeLinks(value as String)
                    splitExamine(text, extras, key, "", false)
                }
                else -> return@forEach
            }
        }
    }

    private fun splitLines(text: String): List<String> {
        val text = formatLineBreaks(text).replace(",", "<br>").replace("/", "<br>")
        val list = text.split("<br>")
        list.flatMap { text ->
            when {
                text.contains("Level ") -> levelRegex.findAll(text).map { it.groupValues.last() }.toList()
                text.contains("(level", true) -> levelRegex2.findAll(text).map { it.groupValues.last() }.toList()
                text.contains(levelRegex4) -> levelRegex4.findAll(text).map { it.groupValues.last() }.toList()
                text.contains("level", true) -> levelRegex3.findAll(text).map { it.groupValues.last() }.toList()
                else -> listOf(text)
            }
        }
        return list
    }

    private fun appendRange(extras: MutableMap<String, Any>, text: String, key: String): Boolean {
        val text = text.trim()
        if (text.contains("-")) {
            val first = text.split("-").first().toIntOrNull()
//            val second = text.split("-").last().toIntOrNull()
            if (first != null) { // && second != null
                extras.putIfAbsent(key, first)
                return true
            }
        } else {
            return appendInt(extras, key, text)
        }
        return false
    }
}

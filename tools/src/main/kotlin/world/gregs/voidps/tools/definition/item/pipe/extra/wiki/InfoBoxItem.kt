package world.gregs.voidps.tools.definition.item.pipe.extra.wiki

import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.engine.data.definition.DefinitionsDecoder.Companion.removeTags
import world.gregs.voidps.engine.data.definition.DefinitionsDecoder.Companion.toIdentifier
import world.gregs.voidps.engine.entity.item.ItemKept
import world.gregs.voidps.engine.entity.item.ItemUse
import world.gregs.voidps.tools.Pipeline
import world.gregs.voidps.tools.definition.item.Extras
import world.gregs.voidps.tools.definition.item.pipe.page.PageCollector
import world.gregs.voidps.tools.wiki.model.Infobox.indexSuffix
import world.gregs.voidps.tools.wiki.model.Infobox.splitByVersion
import world.gregs.voidps.tools.wiki.model.WikiPage
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class InfoBoxItem(val revision: LocalDate) : Pipeline.Modifier<Extras> {

    override fun modify(content: Extras): Extras {
        val (builder, extras) = content
        val (id, name, page, _, rs3, _) = builder
        // Prioritise rs3 pages first as they have better data formats and names
        if (rs3 != null) {
            processRs3(extras, id, rs3, builder)
        }
        if (page != null) {
            processRs2(extras, page)
        }
        if (builder.uid.isEmpty()) {
            builder.uid = toIdentifier(name)
        }
        return content
    }

    private fun processRs3(extras: MutableMap<String, Any>, id: Int, page: WikiPage, builder: PageCollector) {
        splitByVersion(page, "infobox item", id, false) { template, suffix ->
            template.forEach { (key, value) ->
                if (value is ArrayList<*>) {
                    println("Unknown al $value")
                    return@forEach
                }
                if (key.startsWith("weight$suffix")) {
                    extras.putIfAbsent(key.removeSuffix(suffix), (value as? String)?.toDoubleOrNull() ?: 0.0)
                    return@forEach
                }
                when (key) {
                    "name$suffix" -> {
                        val version = template["version$suffix"] as? String
                        val uid = toIdentifier(page.title.replace("(historical)", "").trim())
                        if (version != null) {
                            builder.uid = uid.appendSuffix("_${toIdentifier(version)}")
                            replaceRs3Names(builder, page, version)
                        } else if (builder.rs3Idd) {
                            builder.uid = uid
                            replaceRs3Names(builder, page, "")
                        }
                    }
                    "AKA$suffix" -> extras.putIfAbsent(key.removeSuffix(suffix).lowercase(), removeLinks(value as String))
                    "tradeable$suffix", "edible$suffix", "bankable$suffix", "stacksinbank$suffix" -> {
                        val text = value as String
                        extras.putIfAbsent(key.removeSuffix(suffix).replace("stacksinbank", "bank_stacks"), text.equals("yes", true))
                    }
                    "examine$suffix" -> {
                        val t = value as? String
                        if (t == null) {
                            println("Unknown examine")
                        } else {
                            val text = removeLinks(t).replace("adrenaline", "recover special").replace(usedInRegex, "").trim()
                            splitExamine(text, extras, key, suffix, false)
                        }
                    }
                    "destroy$suffix" -> {
                        val text = removeLinks(value as String)
                        splitExamine(text, extras, key, suffix, false)
                    }
                    "restriction$suffix" -> {
                        val text = value as String
                        val use = if (text == "removed") {
                            val removal = template["removal"] as? String
                            if (removal.isNullOrBlank() || LocalDate.parse(removeLinks(removal), formatter).isBefore(revision)) {
                                ItemUse.Removed
                            } else {
                                ItemUse.Surface
                            }
                        } else {
                            ItemUse.valueOf(text.lowercase().toSentenceCase())
                        }
                        extras.putIfAbsent("use", use)
                    }
                    "kept$suffix" -> {
                        val tradeable = (template["tradeable$suffix"] as? String)?.equals("yes", true) ?: false
                        val text = value as String
                        val kept = when (text.lowercase()) {
                            "always" -> ItemKept.Wilderness
                            "alwaysinclwild", "safe" -> ItemKept.Always
                            "reclaimable" -> if (tradeable) ItemKept.Never else ItemKept.Reclaim
                            "dropped" -> ItemKept.Never
                            "never" -> ItemKept.Vanish
                            else -> ItemKept.Vanish
                        }
                        extras.putIfAbsent(key.removeSuffix(suffix), kept)
                    }
                    else -> return@forEach
                }
            }
        }
    }

    private fun replaceRs3Names(builder: PageCollector, page: WikiPage, suffix: String) {
        if (builder.uid.isNotEmpty()) {
            if (isRs3Name(page.title) || page.title.contains("adrenaline", true)) {
                builder.uid = toIdentifier(builder.name.appendSuffix(" ${toIdentifier(suffix)}").trim())
            }
        }
    }

    private fun String.appendSuffix(suffix: String): String = if (!endsWith(suffix)) "$this$suffix" else this

    private fun processRs2(extras: MutableMap<String, Any>, page: WikiPage) {
        val template = page.getTemplateMap("infobox item") ?: return
        template.forEach { (key, value) ->
            if (value is ArrayList<*>) {
                println("Unknown al $value")
                return@forEach
            }
            when (key) {
                "destroy", "examine" -> {
                    val examine = removeLinks(value as String)
                    if (examine.isBlank()) {
                        return@forEach
                    }
                    val override = isRs3Name(page.title) || isRs3Examine(examine)
                    splitExamine(examine, extras, key, "", override)
                }
                else -> return@forEach
            }
        }
    }

    companion object {
        val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy")!!
        private val usedInRegex = "Used (?:in|with) .*?\\([0-9 &,]+\\).*?\\.".toRegex()

        private val linkNameRegex = "\\[\\[(.*?)]]".toRegex()

        private const val LINE_BREAK = "<br>"

        private val removeParentheses = "\\(.*?\\)\\s?".toRegex()
        private val splitByColon = "(?:\\.|<br>|!|\\?)('?.*?:'?(?:\\s+)?)".toRegex()
        private val splitByParentheses = "[.!?]((?:\\s+)?\\(.*?\\)(?:\\s+)?)".toRegex()

        private fun isRs3Examine(string: String): Boolean = string.contains("Used ")

        private fun isRs3Name(string: String): Boolean = string.endsWith("dragon claw", true) ||
            string.endsWith("black claw", true) ||
            string.endsWith("white claw", true) ||
            string.endsWith("torag's hammer", true) ||
            string.startsWith("blessed dragonhide", true) ||
            string.contains("shieldbow", true) ||
            string.contains("chargebow", true) ||
            string.endsWith("arrowheads", true) ||
            string.contains("hallowe'en", true) ||
            string.equals("dragon helm", true) ||
            string.equals("oxidised helm", true) ||
            string.startsWith("void knight ", true)

        fun removeLinks(text: String): String = text.replace(linkNameRegex) {
            val result = it.groupValues[1]
            if (result.contains("|")) {
                result.split("|").last()
            } else {
                result
            }
        }

        fun splitExamine(text: String, extras: MutableMap<String, Any>, key: String, suffix: String, override: Boolean) {
            val text = removeBold(formatLineBreaks(text).replace("\"", ""))
            if (text.contains(LINE_BREAK) || text.contains(":") || text.contains("(")) {
                val parts = when {
                    text.contains(":") -> {
                        text
                            .splitByMatch(splitByColon)
                            .flatMap { line ->
                                splitParentheses(line)
                            }
                    }
                    text.contains(LINE_BREAK) -> text.split(LINE_BREAK).map { it.trim() }
                    text.contains("(") -> splitParentheses(text)
                    else -> throw RuntimeException("Unknown split '$text'")
                }
                var index = 0
                for (it in parts) {
                    val line = getLine(it)
                    if (line != null) {
                        val key = indexSuffix(key.removeSuffix(suffix), index)
                        if (!append(extras, line, key, override)) {
                            break
                        }
                        index++
                    }
                }
            } else if (text.isNotBlank()) {
                val line = getLine(text) ?: return
                append(extras, line, key.removeSuffix(suffix), override)
            }
        }

        fun formatLineBreaks(text: String) = text
            .replace("<br/>", LINE_BREAK)
            .replace("<br />", LINE_BREAK)
            .replace("\n", " ")
            .replace("\\n", LINE_BREAK)
            .replace("*", LINE_BREAK)
            .replace(" / ", LINE_BREAK)
            .replace(" OR ", LINE_BREAK)
            .replace("./", ".$LINE_BREAK")

        /**
         * Same as [split] but only splits by captured group not full match
         */
        private fun String.splitByMatch(regex: Regex): List<String> {
            val result = mutableListOf<String>()
            var start = 0
            regex.findAll(this).forEach {
                val group = it.groups.last() ?: return@forEach
                result.add(substring(start, group.range.first))
                start = group.range.last + 1
            }
            if (start != length) {
                result.add(substring(start, length))
            }
            return result
        }

        private fun splitParentheses(text: String) = text
            .replace(LINE_BREAK, "")
            .splitByMatch(splitByParentheses)
            .map { part -> part.split(":").last().trim() }
            .filter { it.isNotBlank() && it != "." }

        private fun getLine(text: String): String? {
            var line = text.trim()
            if (line.endsWith(":") || line.isBlank() || line == ".") {
                return null
            }
            if (line.contains(":")) {
                line = line.split(":").last()
            }
            return removeLinks(removeTags(line)).trim()
        }

        private fun append(extras: MutableMap<String, Any>, line: String, key: String, override: Boolean): Boolean {
            if (!override && extras.containsKey(key)) {
                return false
            }
            extras[key] = line.replace(removeParentheses, "").trim()
            return true
        }

        fun removeBold(text: String) = text.replace("'''", "").replace("''", "")
        fun removeParentheses(text: String) = text.replace(removeParentheses, "").replace("(", "").replace(")", "")
    }
}

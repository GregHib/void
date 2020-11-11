package rs.dusk.tools.definition.item.pipe.extra.wiki

import rs.dusk.tools.Pipeline
import rs.dusk.tools.definition.item.Extras

class ItemBonuses : Pipeline.Modifier<Extras> {

    override fun modify(content: Extras): Extras {
        val (builder, extras) = content
        val (id, _, page, _, rs3, _, _, _, uid) = builder
        val template = page?.getTemplateMap("infobox bonuses") ?: return content
        template.forEach { (key, value) ->
            when (key) {
                "astab", "aslash", "acrush", "amagic", "arange", "dstab", "dslash", "dcrush", "dmagic", "drange", "dsummon", "str", "rangestr" -> {
                    val text = (value as String).replace("+", "").replace("<br />", "")
                    val v = text.toDoubleOrNull()
                    if (v != null) {
                        extras[key] = v
                    } else if (text.contains("to")) {
                        val itemTemplate = rs3?.getTemplateMap("infobox item")
                        val versions = itemTemplate?.entries?.count { it.key.startsWith("version") } ?: 0
                        val versionEntry = itemTemplate?.entries?.firstOrNull { (it.value as String).toIntOrNull() == id }
                        val version = versionEntry?.key?.removePrefix("id")?.toIntOrNull()
                        if (version != null && versions > 0) {
                            val parts = text.split(" to ")
                            val range = parts.first().trim().replace("+", "").toInt()..parts.last().trim().replace("+", "").toInt()
                            val dif = range.last - range.first
                            val step = dif / (versions - 2.0)// -1 because new/full crystal bow have same values
                            val expected = range.first + (versions - version) * step
                            extras[key] = expected
                        }
                    } else {
                        println("Unknown bonus $id $uid $key $value")
                    }
                }
                "absorbmelee", "absorbmagic", "absorbranged", "magicdamage", "prayer" -> {
                    val text = (value as String).replace("%", "")
                    val v = text.toIntOrNull()
                    when {
                        v != null -> extras[key] = v
                        text.contains("trimmed") -> {
                            extras[key] = if(uid.endsWith("_t")) {
                                4
                            } else {
                                text.replace(" (+4 trimmed)", "").toInt()
                            }
                        }
                        value.isNotBlank() -> println("Unknown bonus $id $uid $key $value")
                    }
                }
                else -> return@forEach
            }
        }
        return content
    }
}
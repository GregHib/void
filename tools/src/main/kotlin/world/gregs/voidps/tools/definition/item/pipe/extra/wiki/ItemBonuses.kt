package world.gregs.voidps.tools.definition.item.pipe.extra.wiki

import world.gregs.voidps.tools.Pipeline
import world.gregs.voidps.tools.definition.item.Extras

class ItemBonuses : Pipeline.Modifier<Extras> {

    val keys = mapOf(
        "astab" to "stab",
        "aslash" to "slash",
        "acrush" to "crush",
        "amagic" to "magic",
        "arange" to "range",
        "dstab" to "stab_def",
        "dslash" to "slash_def",
        "dcrush" to "crush_def",
        "dmagic" to "magic_def",
        "drange" to "range_def",
        "dsummon" to "summoning_def",
        "str" to "str",
        "rangestr" to "range_str",
        "absorbmelee" to "absorb_melee",
        "absorbmagic" to "absorb_magic",
        "absorbranged" to "absorb_range",
        "magicdamage" to "magic_damage",
        "prayer" to "prayer",
    )

    override fun modify(content: Extras): Extras {
        val (builder, extras) = content
        val (id, _, page, _, rs3, _, _, _, uid) = builder
        val template = page?.getTemplateMap("infobox bonuses") ?: return content
        template.forEach { (key, value) ->
            if (value is ArrayList<*>) {
                println("Unknown al $value")
                return@forEach
            }
            when (key) {
                "astab", "aslash", "acrush", "amagic", "arange", "dstab", "dslash", "dcrush", "dmagic", "drange", "dsummon", "str", "rangestr" -> {
                    val text = (value as String).replace("+", "").replace("<br />", "")
                    val v = text.toDoubleOrNull()
                    if (v != null) {
                        extras[keys[key]!!] = v
                    } else if (text.contains("to")) {
                        val itemTemplate = rs3?.getTemplateMap("infobox item")
                        val versions = itemTemplate?.entries?.count { it.key.startsWith("version") } ?: 0
                        val versionEntry = itemTemplate?.entries?.firstOrNull { (it.value as String).toIntOrNull() == id }
                        val version = versionEntry?.key?.removePrefix("id")?.toIntOrNull()
                        if (version != null && versions > 0) {
                            val parts = text.split(" to ")
                            val range = parts.first().trim().replace("+", "").toInt()..parts.last().trim().replace("+", "").toInt()
                            val dif = range.last - range.first
                            val step = dif / (versions - 2.0) // -1 because new/full crystal bow have same values
                            val expected = range.first + (versions - version) * step
                            extras[keys[key]!!] = expected
                        }
                    } else {
                        println("Unknown bonus $id $uid $key $value")
                    }
                }
                "absorbmelee", "absorbmagic", "absorbranged", "magicdamage", "prayer" -> {
                    val text = (value as String).replace("%", "")
                    val v = text.toIntOrNull()
                    when {
                        v != null -> extras[keys[key]!!] = v
                        text.contains("trimmed") -> {
                            extras[keys[key]!!] = if (uid.endsWith("_t")) {
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

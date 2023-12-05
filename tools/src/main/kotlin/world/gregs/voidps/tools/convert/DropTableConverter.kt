package world.gregs.voidps.tools.convert

import world.gregs.voidps.engine.data.definition.DefinitionsDecoder.Companion.toIdentifier
import world.gregs.voidps.engine.entity.item.drop.DropTable
import world.gregs.voidps.engine.entity.item.drop.ItemDrop
import world.gregs.voidps.engine.entity.item.drop.TableType
import world.gregs.yaml.Yaml
import world.gregs.yaml.write.YamlWriterConfiguration

object DropTableConverter {

    @JvmStatic
    fun main(args: Array<String>) {
        val string = """
{{DropsTableHead}}
{{DropsLine|name=Key (elite)|quantity=1|rarity=Always|raritynotes=<ref group=d>The key is only dropped when completing an elite clue scroll asking you to kill the King Black Dragon.</ref>|gemw=No}}
{{DropsLine|name=Kbd heads|quantity=1|rarity=1/128|gemw=No}}
{{DropsLineClue|type=elite|rarity=1/450}}
{{DropsLine|name=Prince black dragon|quantity=1|rarity=1/3000|gemw=No}}
{{DropsLine|name=Draconic visage|quantity=1|rarity=1/5000}}
{{DropsTableBottom}}
        """.trimIndent()
        val all = mutableListOf<DropTable>()
        var builder = DropTable.Builder()
        var name = ""
        for (line in string.lines()) {
            if (line.startsWith("=")) {
                name = toIdentifier(line.replace("=", ""))
            } else if (line.startsWith("{{DropsLine|")) {
                process(builder, line)
            } else if (line.startsWith("{{DropsTableBottom")) {
                val table = builder.build()
                all.add(table)
                builder = DropTable.Builder()
            }
        }
        println(mapper.writeToString(mapOf("drop_table" to combine(all)), writer))
    }

    private fun combine(all: MutableList<DropTable>): DropTable {
        val parent = DropTable.Builder()
        if (all.size == 1) {
            return all.first()
        }
        val always = all.firstOrNull { it.roll == 1 }
        parent.withType(TableType.All)
        if (always != null) {
            parent.addDrop(always)
        }
        val table = DropTable.Builder()
        for (child in all) {
            if (child.roll == 1) {
                continue
            }
            table.withType(child.type)
            table.withRoll(child.roll)
            for (drop in child.drops) {
                table.addDrop(drop)
            }
        }
        parent.addDrop(table.build())
        return parent.build()
    }

    private val writer = object : YamlWriterConfiguration() {
        override fun write(value: Any?, indent: Int, parentMap: String?): Any? {
            return when (value) {
                is ItemDrop -> {
                    val map = mutableMapOf<String, Any>("id" to value.id)
                    if (value.chance != 1) {
                        map["chance"] = value.chance
                    }
                    if (value.amount.first == value.amount.last) {
                        val amount = value.amount.first
                        if (amount != 1) {
                            map["amount"] = amount
                        }
                    } else {
                        map["amount"] = value.amount.toString()
                    }
                    if (value.members) {
                        map["members"] = true
                    }
                    super.write(map, indent, parentMap)
                }
                is DropTable -> {
                    val map = mutableMapOf<String, Any>()
                    if (value.type != TableType.First) {
                        map["type"] = value.type.name.lowercase()
                    }
                    if (value.roll != 1) {
                        map["roll"] = value.roll
                    }
                    map["drops"] = value.drops
                    super.write(map, indent, parentMap)
                }
                else -> super.write(value, indent, parentMap)
            }
        }
    }
    private val mapper = Yaml()

    fun process(builder: DropTable.Builder, string: String) {
        val parts = string.split("|", "=").drop(1)

        val map = mutableMapOf<String, String>()
        for (i in 0 until parts.lastIndex step 2) {
            map[parts[i].lowercase()] = parts[i + 1].removeSuffix("}}").removePrefix("{{")
        }

        assert(map.containsKey("name"))
        assert(map.containsKey("rarity"))

        var id = toIdentifier(map.getValue("name"))
        val members = toIdentifier(map.getOrDefault("namenotes", "")) == "m"
        val quantity = map["quantity"] ?: "0"
        val rarity = map.getValue("rarity")
        val (chance, total) = if (rarity.contains("/")) {
            rarity.split("/")
        } else {
            when (rarity) {
                "Always" -> listOf("1", "1")
                "Very common" -> listOf("1", "8")
                "Common" -> listOf("1", "16")
                "Semi-common" -> listOf("1", "32")
                "Uncommon" -> listOf("1", "64")
                "Semi-rare" -> listOf("1", "128")
                "Rare" -> listOf("1", "256")
                "Very rare" -> listOf("1", "512")
                else -> throw IllegalArgumentException("Unknown rarity '${rarity}'")
            }
        }

        if (quantity.endsWith("(noted)")) {
            id = "${id}_noted"
        }

        val (low, high) = if (quantity.contains("-")) {
            quantity.split("-")
        } else {
            val amount = quantity.removeSuffix(" (noted)")
            listOf(amount, amount)
        }
        builder.withRoll(total.toInt())
        builder.addDrop(ItemDrop(id, low.toInt()..high.toInt(), chance.toInt(), members))
    }
}
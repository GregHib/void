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
{{DropsLine|Name=Coins|Quantity=5-32|Rarity=Common|gemw=no|Image=Coins 1000.png}}
{{DropsLine|Name=Cooked meat|Quantity=1|Rarity=Common}}
{{DropsLine|Name=Amulet mould|Quantity=1|Rarity=Uncommon}}
{{DropsLine|Name=Bear fur|Quantity=1|Rarity=Uncommon}}
{{DropsLine|Name=Beer|Quantity=1|Rarity=Uncommon}}
{{DropsLine|Name=Flier|Quantity=1|Rarity=Uncommon|gemw=no|Image=Flier.png}}
{{DropsLine|Name=Clue scroll (easy)|Quantity=1|Rarity=Rare|gemw=no|Image=Clue scroll.png}}
{{DropsLine|Name=Uncut sapphire|Quantity=1|Rarity=Rare}}
{{DropsLine|Name=Uncut emerald|Quantity=1|Rarity=Rare}}
{{DropsLine|Name=Uncut ruby|Quantity=1|Rarity=Rare}}
{{DropsLine|Name=Uncut diamond|Quantity=1|Rarity=Rare}}
{{DropsLine|Name=Uncut dragonstone|Quantity=1|Rarity=Rare}}
{{DropsLine|Name=Loop half of a key|Quantity=1|Rarity=Very rare}}
        """.trimIndent()
        val all = mutableListOf<DropTable>()
        var builder = DropTable.Builder()
        for (line in string.lines()) {
            if (line.startsWith("=")) {
                val name = toIdentifier(line.replace("=", ""))
            } else if (line.startsWith("{{DropsLine|")) {
                process(builder, line)
            } else if (line.startsWith("{{DropsTableBottom")) {
                val table = builder.build()
                all.add(table)
                builder = DropTable.Builder()
            }
        }
        all.add(builder.build())
        println(Yaml().writeToString(mapOf("drop_table" to combine(all)), writer))
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
        override fun explicit(list: List<*>, indent: Int, parentMap: String?): Boolean {
            return false
        }

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

        builder.withRoll(total.toInt())

        if (quantity.contains("-")) {
            val (low, high) = quantity.split("-")
            builder.addDrop(ItemDrop(id, low.toInt()..high.toInt(), chance.toInt(), members))
        } else if(quantity.contains(",")){
            val values = quantity.split(",").map { it.toInt() }
            val low = values.min()
            val high = values.max()
            builder.addDrop(ItemDrop(id, low..high, chance.toInt(), members))
        } else {
            val amount = quantity.removeSuffix(" (noted)").toInt()
            builder.addDrop(ItemDrop(id, amount..amount, chance.toInt(), members))
        }
    }
}
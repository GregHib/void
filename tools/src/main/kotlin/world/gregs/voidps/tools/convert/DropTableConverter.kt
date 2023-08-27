package world.gregs.voidps.tools.convert

import world.gregs.voidps.engine.data.definition.DefinitionsDecoder.Companion.toIdentifier
import world.gregs.voidps.engine.entity.item.drop.DropTable
import world.gregs.voidps.engine.entity.item.drop.ItemDrop
import world.gregs.yaml.Yaml
import world.gregs.yaml.write.YamlWriterConfiguration

object DropTableConverter {

    @Suppress("USELESS_CAST")
    @JvmStatic
    fun main(args: Array<String>) {
        val string = """
===Other===
{{DropsTableHead}}
{{DropsLine|Name=Nothing|Rarity=38/128}}
{{DropsLine|Name=Hammer|Quantity=1|Rarity=15/128}}
{{DropsLine|Name=Goblin book|Namenotes={{(m)}}|Quantity=1|Rarity=2/128|gemw=No}}
{{DropsLine|Name=Goblin mail|Quantity=1|Rarity=5/128}}
{{DropsLine|Name=Chef's hat|Quantity=1|Rarity=3/128}}
{{DropsLine|Name=Beer|Quantity=1|Rarity=2/128}}
{{DropsLine|Name=Brass necklace|Quantity=1|Rarity=1/128}}
{{DropsLine|Name=Air talisman|Quantity=1|Rarity=1/128}}
{{DropsTableBottom}}
        """.trimIndent()
        val builder = DropTable.Builder()
        var name = ""
        for (line in string.lines()) {
            if (line.startsWith("=")) {
                name = toIdentifier(line.replace("=", ""))
            } else if (line.startsWith("{{DropsLine|")) {
                process(builder, line)
            }
        }

        val writer = object : YamlWriterConfiguration() {
            override fun write(value: Any?, indent: Int, parentMap: String?): Any? {
                return when (value) {
                    is ItemDrop -> {
                        val map = mutableMapOf("id" to value.id, "amount" to value.amount.toString(), "chance" to value.chance)
                        if (value.members) {
                            map["members"] = true
                        }
                        super.write(map, indent, parentMap)
                    }
                    is DropTable -> super.write(mapOf("type" to value.type, "roll" to value.roll, "drops" to value.drops), indent, parentMap)
                    else -> super.write(value, indent, parentMap)
                }
            }
        }
        val mapper = Yaml()

        val table = builder.build()

        println(mapper.writeToString(mapOf(name to table), writer))
    }

    fun process(builder: DropTable.Builder, string: String) {
        val parts = string.split("|", "=").drop(1)

        val map = mutableMapOf<String, String>()
        for (i in 0 until parts.lastIndex step 2) {
            map[parts[i].lowercase()] = parts[i + 1].removeSuffix("}}").removePrefix("{{")
        }

        assert(map.containsKey("name"))
        assert(map.containsKey("rarity"))

        println(map)
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
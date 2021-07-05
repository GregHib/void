package world.gregs.voidps.tools.convert

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import world.gregs.voidps.engine.entity.definition.DefinitionsDecoder.Companion.toIdentifier

object DropTableConverter {

    interface Drop

    class RangeSerializer : JsonSerializer<IntRange>() {
        override fun serialize(value: IntRange, jsonGenerator: JsonGenerator, serializerProvider: SerializerProvider) {
            jsonGenerator.writeObject("${value.first}-${value.last}")
        }
    }

    class ChanceSerializer : JsonSerializer<IntRange>() {
        override fun serialize(value: IntRange, jsonGenerator: JsonGenerator, serializerProvider: SerializerProvider) {
            jsonGenerator.writeObject("${value.first}/${value.last}")
        }
    }

    data class QuantityDrop(
        val name: String,
        @get:JsonSerialize(using = RangeSerializer::class)
        val quantity: IntRange,
        @get:JsonSerialize(using = ChanceSerializer::class)
        val chance: IntRange,
    ) : Drop

    data class AmountDrop(
        val name: String,
        val amount: Int,
        @get:JsonSerialize(using = ChanceSerializer::class)
        val chance: IntRange,
    ) : Drop

    data class DropTable(
        @JsonIgnore
        val name: String,
        val drops: List<Drop>
    ) : Drop {
        class Builder {
            private var name: String? = null
            private val drops = mutableListOf<Drop>()

            fun withName(name: String): Builder {
                this.name = name
                return this
            }

            fun addDrop(drop: Drop): Builder {
                this.drops.add(drop)
                return this
            }

            fun build(): DropTable {
                assert(name != null) { "Drop table name cannot be null." }
                return DropTable(name!!, drops)
            }
        }
    }

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
        for (line in string.lines()) {
            if (line.startsWith("=")) {
                builder.withName(toIdentifier(line.replace("=", "")))
            } else if (line.startsWith("{{DropsLine|")) {
                process(builder, line)
            }
        }

        val mapper = ObjectMapper(YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER).apply {
            enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
            disable(YAMLGenerator.Feature.SPLIT_LINES)
        })

        val table = builder.build()

        println(mapper.writeValueAsString(mapOf(table.name to table)))
    }

    fun process(builder: DropTable.Builder, string: String) {
        val parts = string.split("|", "=").drop(1)

        val map = mutableMapOf<String, String>()
        for (i in 0 until parts.lastIndex step 2) {
            map[parts[i]] = parts[i + 1].removeSuffix("}}")
        }

        assert(map.containsKey("Name"))
        assert(map.containsKey("Rarity"))

        var name = toIdentifier(map.getValue("Name"))
        val quantity = map["Quantity"] ?: "0"
        val rarity = map.getValue("Rarity")
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
            name = "${name}_noted"
        }

        if (quantity.contains("-")) {
            val (low, high) = quantity.split("-")
            builder.addDrop(QuantityDrop(name, low.toInt()..high.toInt(), chance.toInt()..total.toInt()))
        } else {
            builder.addDrop(AmountDrop(name, quantity.removeSuffix(" (noted)").toInt(), chance.toInt()..total.toInt()))
        }
    }
}
package world.gregs.voidps.tools.convert

import world.gregs.config.*
import world.gregs.voidps.engine.data.definition.DefinitionsDecoder.Companion.toIdentifier
import world.gregs.voidps.engine.entity.item.drop.DropTable
import world.gregs.voidps.engine.entity.item.drop.ItemDrop
import world.gregs.voidps.engine.entity.item.drop.TableType
import java.util.*

object DropTableConverter {

    @JvmStatic
    fun main(args: Array<String>) {
        val string = """
===Coins===
{{DropsTableHead|dropversion=Regular}}
{{DropsLine|name=Coins|quantity=37|rarity=19/128}}
{{DropsLine|name=Coins|quantity=2|rarity=11/128|raritynotes=<ref group="d">Drop rate is decreased to 8/128 in [[members]] worlds.</ref>|altrarity=8/128}}
{{DropsLine|name=Coins|quantity=119|rarity=10/128}}
{{DropsLine|name=Coins|quantity=300|rarity=2/128}}
{{DropsTableBottom}}
{{Reflist|group=d}}

        """.trimIndent()
        val all = mutableListOf<DropTable>()
        var builder = DropTable.Builder()
        for (line in string.lines()) {
            if (line.startsWith("=")) {
                val name = toIdentifier(line.replace("=", ""))
            } else if (line.startsWith("{{DropsLineClue|")) {
                process(builder, line.replace("Clue|type=", "|name=").replace("|rarity=", " clue scroll|quantity=1|rarity="))
            } else if (line.startsWith("{{DropsLine|")) {
                process(builder, line)
            } else if (line.startsWith("{{DropsTableBottom")) {
                val table = builder.build()
                all.add(table)
                builder = DropTable.Builder()
            }
        }
        val element = builder.build()
        if (element.drops.isNotEmpty()) {
            all.add(element)
        }
        println(Config.stringWriter {
            val root = combine(all)
            val queue = LinkedList<Pair<String, DropTable>>()
            queue.add("drop_table" to root)
            var i = 0
            while (queue.isNotEmpty()) {
                val (name, table) = queue.poll()
                writeSection(name)
                if (table.type == TableType.All) {
                    writePair("type", "all")
                }
                if (table.roll != 1) {
                    writePair("roll", table.roll)
                }
                writeKey("drops")
                write("[\n")
                for (drop in table.drops) {
                    if (drop is ItemDrop) {
                        write(drop)
                    } else if (drop is DropTable) {
                        val sub = "sub_table_${i++}"
                        write("  { table = \"$sub\"")
                        if (drop.roll != 1) {
                            write(", roll = ${drop.roll}")
                        }
                        write(" },\n")
                        queue.add(sub to drop)
                    }
                }
                write("]\n\n")
            }
        })
    }

    private fun ConfigWriter.write(value: ItemDrop) {
        write("  { ")
        writeKey("id")
        writeValue(value.id)
        if (value.amount != 1..1) {
            write(", ")
            if (value.amount.first == value.amount.last) {
                writeKey("amount")
                writeValue(value.amount.first)
            } else {
                writeKey("min")
                writeValue(value.amount.first)
                write(", ")
                writeKey("max")
                writeValue(value.amount.last)
            }
        }
        if (value.chance != 1) {
            write(", ")
            writeKey("chance")
            writeValue(value.chance)
        }
        if (value.predicate != null) {
            write(", ")
            writeKey("members")
            writeValue(true)
        }
        write(" },\n")
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
        val roll = all.maxOf { it.roll }
        table.withRoll(roll)
        for (child in all) {
            if (child.roll == 1) {
                continue
            }
            table.withType(child.type)
            val multiplier = roll / child.roll
            for (drop in child.drops) {
                if (drop is ItemDrop) {
                    table.addDrop(drop.copy(chance = drop.chance * multiplier))
                }
            }
        }
        parent.addDrop(table.build())
        return parent.build()
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
            builder.addDrop(ItemDrop(id, low.toInt()..high.toInt(), chance.toInt()))
        } else if (quantity.contains(",")) {
            val values = quantity.split(",").map { it.trim().toInt() }
            val low = values.min()
            val high = values.max()
            builder.addDrop(ItemDrop(id, low..high, chance.toInt()))
        } else {
            val amount = quantity.removeSuffix(" (noted)").toInt()
            builder.addDrop(ItemDrop(id, amount..amount, chance.toInt()))
        }
    }
}
package world.gregs.voidps.tools.convert

import world.gregs.config.*
import world.gregs.voidps.engine.data.definition.DefinitionsDecoder.Companion.toIdentifier
import world.gregs.voidps.engine.entity.item.drop.Drop
import world.gregs.voidps.engine.entity.item.drop.DropTable
import world.gregs.voidps.engine.entity.item.drop.ItemDrop
import world.gregs.voidps.engine.entity.item.drop.TableType
import world.gregs.yaml.write.YamlWriterConfiguration
import java.util.*

object DropTableConverter {

    @JvmStatic
    fun main(args: Array<String>) {
        val string = """
====Normal mode, P2P world {{Members|yes}}====
{{DropsTableHead|version=Normal}}
{{DropsLine|name=Long, sharp claws|quantity=1|rarity=Common|raritynotes={{DropNote|Only during the [[Fur 'n Seek/Wish list|Fur 'n Seek wish list]].}}|gemw=no}}
{{DropsLine|name=Numbing root|quantity=3-6|rarity=46/520|raritynotes={{DropNote|name=rare unique|There is a 1/10 chance to roll onto the mole's unique items table. The rates listed are the overall rate for each specific item, including the 1/10 chance to receive any rare unique item at all. The drop rates of rare items depends on if players are on free-to-play worlds and if fighting in hard mode.}}|citations={{NamedRef|revealed rates}}}}
{{DropsLine|name=Clingy mole|quantity=1|rarity=5/520|raritynotes={{DropNote|name=rare unique}}|citations={{NamedRef|revealed rates}}}}
{{DropsLine|name=Dragon 2h sword|quantity=1|rarity=1/520|raritynotes={{DropNote|name=rare unique}}|citations={{NamedRef|revealed rates}}}}
{{DropsLine|name=Sealed clue scroll (hard)|quantity=1|rarity=1/128}}
{{DropsLine|name=Sealed clue scroll (elite)|quantity=1|rarity=99/128000|raritynotes={{DropNote|This drop only attempts to roll should you fail to roll a hard clue. Should you get this item, there's a 1% chance it'll be upgraded to a [[sealed clue scroll (master)]].}}}}
{{DropsLine|name=Sealed clue scroll (master)|quantity=1|rarity=1/128000}}
{{DropsLine|name=Long bone|quantity=1|rarity=1/400}}
{{DropsLine|name=Curved bone|quantity=1|rarity=1/5000}}
{{DropsLine|name=Starved ancient effigy|quantity=1|rarity=Rare}}
{{DropsLine|name=Rotten fang|quantity=1|rarity=1/2500|altrarity=10/2500|altraritydash=yes|gemw=no|alch=no|raritynotes={{DropNote|name=threshold|Base drop rate of 1/2,500, with a [[Boss pets#Pets unlocked by drops|threshold of 500]].}}|citations={{CiteForum|author = Mod Ryan|url = http://services.runescape.com/m=forum/a=13/sl=0/forums.ws?373,374,807,65550979,goto,1|archivedate=9 February 2015|archiveurl=https://archive.ph/ZkWyc|title = Boss pets - Drop rates!|forum = Ninja Team|postdate = 9 January 2015|name = "pet drop rate"}}}}
{{DropsTableBottom}}


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
                        if(drop.roll != 1) {
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
        if (value.members) {
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
        } else if (quantity.contains(",")) {
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
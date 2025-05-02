package world.gregs.voidps.tools.convert

import net.pearx.kasechange.toSnakeCase
import world.gregs.config.*
import world.gregs.voidps.engine.data.definition.DefinitionsDecoder.Companion.toIdentifier
import world.gregs.voidps.engine.entity.item.drop.TableType
import java.util.*

object DropTableConverter {

    @JvmStatic
    fun main(args: Array<String>) {
        val string = """
===100%===
{{DropsTableHead}}
{{DropsLine|name=Bones|quantity=1|rarity=Always}}
{{DropsTableBottom}}

===Weapons and armour===
{{DropsTableHead}}
{{DropsLine|name=Bronze axe|quantity=1|rarity=3/128}}
{{DropsLine|name=Bronze scimitar|quantity=1|rarity=1/128}}
{{DropsLine|name=Bronze spear|namenotes={{(m)}}|quantity=1|rarity=9/128}}
{{DropsTableBottom}}

===Runes and ammunition===
{{DropsTableHead}}
{{DropsLine|name=Bronze arrow|quantity=7|rarity=3/128}}
{{DropsLine|name=Mind rune|quantity=2|rarity=3/128}}
{{DropsLine|name=Earth rune|quantity=4|rarity=3/128}}
{{DropsLine|name=Body rune|quantity=2|rarity=3/128}}
{{DropsLine|name=Bronze javelin|namenotes={{(m)}}|quantity=5|rarity=2/128}}
{{DropsLine|name=Chaos rune|quantity=1|rarity=1/128}}
{{DropsLine|name=Nature rune|quantity=1|rarity=1/128}}
{{DropsTableBottom}}

===Herbs===
{{HerbDropTableInfo|2/128}}
{{DropsTableHead}}
{{HerbDropLines|2/128|f2p=yes}}
{{DropsTableBottom}}

===Coins===
{{DropsTableHead}}
{{DropsLine|name=Coins|quantity=1|rarity=34/128|gemw=No}}
{{DropsLine|name=Coins|quantity=3|rarity=13/128|gemw=No}}
{{DropsLine|name=Coins|quantity=5|rarity=8/128|gemw=No}}
{{DropsLine|name=Coins|quantity=16|rarity=7/128|gemw=No}}
{{DropsLine|name=Coins|quantity=24|rarity=3/128|gemw=No}}
{{DropsLine|name=Coins|namenotes={{(f)}}|quantity=10|rarity=2/128|raritynotes=<ref group=d>Only dropped in [[free-to-play]] worlds.</ref>|gemw=No}}
{{DropsTableBottom}}
{{Reflist|group=d}}

===Other===
{{DropsTableHead}}
{{DropsLine|name=Hammer|quantity=1|rarity=9/128}}
{{DropsLine|name=Goblin book|namenotes={{(m)}}|quantity=1|rarity=2/128|gemw=No}}
{{DropsLine|name=Goblin mail|quantity=1|rarity=10/128|raritynotes=<ref group=d>Colour received depends on the goblin mail worn.</ref>}}
{{DropsLine|name=Grapes|quantity=1|rarity=1/128}}
{{DropsLine|name=Red cape|quantity=1|rarity=1/128}}
{{DropsLine|name=Tin ore|quantity=1|rarity=1/128}}
{{DropsTableBottom}}
{{Reflist|group=d}}

===Tertiary===
{{DropsTableHead}}
{{DropsLine|name=Goblin skull|namenotes={{(m)}}|quantity=1|rarity=1/4|raritynotes=<ref group=d>Goblin skulls are only dropped during [[Rag and Bone Man I]].</ref>|gemw=No}}
{{DropsLine|name=Ensouled goblin head|namenotes={{(m)}}|quantity=1|rarity=1/30}}
{{DropsLine|name=Clue scroll (beginner)|quantity=1|rarity=1/80|gemw=No}}
{{DropsLineClue|type=easy|rarity=1/128|f2p=yes}}
{{DropsLine|name=Goblin champion scroll|namenotes={{(m)}}|quantity=1|rarity=1/5000|gemw=No}}
{{DropsTableBottom}}
{{Reflist|group=d}}
        """.trimIndent()
        val npc = "stronghold_goblin"
        val all = mutableListOf<Builder>()
        var builder = Builder()
        for (line in string.lines()) {
            if (line.startsWith("=")) {
                val name = toIdentifier(line.replace("=", ""))
                builder.name = name
            } else if (line.startsWith("{{DropsLineClue|")) {
                process(builder, line.replace("Clue|type=", "|name=").replace("|rarity=", " clue scroll|quantity=1|rarity="))
            } else if (line.startsWith("{{DropsLine|")) {
                process(builder, line)
            } else if (line.contains("DropTableInfo")) {
                val parts = line.trim('{', ' ', '}').split('|')
                val name = parts[0].removeSuffix("DropTableInfo").toSnakeCase()
                val moreParts = parts[1].split("/")
                val chance = moreParts[0].toInt()
                val roll = moreParts[1].toInt()
                builder.addDrop(Builder.Drop.table("${name}_drop_table", chance = chance))
                builder.withRoll(roll)
                all.add(builder)
                builder = Builder()
            } else if (line.startsWith("{{DropsTableBottom")) {
                all.add(builder)
                builder = Builder()
            }
        }
        if (builder.drops.isNotEmpty()) {
            all.add(builder)
        }
        val queue = LinkedList<Builder>()
        val parent = Builder()
        parent.name = "${npc}_drop_table"
        queue.add(parent)
        val always = all.firstOrNull { it.name == "100%" }
        if (always != null) {
            always.name = "${npc}_primary"
            all.remove(always)
            if (always.drops.all { it.id == "bones" }) {
                parent.addDrop(Builder.Drop.table("bones"))
            } else if (always.drops.all { it.id == "big_bones" }) {
                parent.addDrop(Builder.Drop.table("big_bones"))
            } else {
                queue.add(always)
                parent.addDrop(Builder.Drop.table(always.name))
            }
        }
        val tertiary = all.firstOrNull { it.name == "tertiary" }
        if (tertiary != null) {
            all.remove(tertiary)
        }
        val combined = Builder(all)
        combined.name = "${npc}_secondary"
        parent.addDrop(Builder.Drop.table(combined.name))
        queue.add(combined)
        if (tertiary != null) {
            tertiary.name = "${npc}_tertiary"
            queue.add(tertiary)
            parent.addDrop(Builder.Drop.table(tertiary.name))
        }
        while (queue.isNotEmpty()) {
            val table = queue.poll()
            table.build()
            println(table.toString())
        }
    }

    fun process(builder: Builder, string: String) {
        val parts = string.split("|", "=").drop(1)

        val map = mutableMapOf<String, String>()
        for (i in 0 until parts.lastIndex step 2) {
            map[parts[i].lowercase()] = parts[i + 1].removeSuffix("}}").removePrefix("{{")
        }

        assert(map.containsKey("name"))
        assert(map.containsKey("rarity"))

        var id = toIdentifier(map.getValue("name"))
        val notes = toIdentifier(map.getOrDefault("namenotes", ""))
        val members = when (notes) {
            "m" -> true
            "f" -> false
            else -> null
        }
        val quantity = map["quantity"] ?: "0"
        val rarity = map.getValue("rarity")
        val (chance, roll) = if (rarity.contains("/")) {
            rarity.split("/").map { it.toInt() }
        } else {
            when (rarity) {
                "Always" -> listOf(1, 1)
                "Very common" -> listOf(1, 8)
                "Common" -> listOf(1, 16)
                "Semi-common" -> listOf(1, 32)
                "Uncommon" -> listOf(1, 64)
                "Semi-rare" -> listOf(1, 128)
                "Rare" -> listOf(1, 256)
                "Very rare" -> listOf(1, 512)
                else -> throw IllegalArgumentException("Unknown rarity '${rarity}'")
            }
        }

        builder.withRoll(roll)

        if (quantity.endsWith("(noted)")) {
            id = "${id}_noted"
        }
        if (id.endsWith("_axe")) {
            id = id.replace("_axe", "_hatchet")
        }
        if (quantity.contains("-")) {
            val (low, high) = quantity.split("-")
            builder.addDrop(Builder.Drop(id, low.toInt()..high.toInt(), chance, roll, members))
        } else if (quantity.contains(",")) {
            val values = quantity.split(",").map { it.trim().toInt() }
            val low = values.min()
            val high = values.max()
            builder.addDrop(Builder.Drop(id, low..high, chance, roll, members))
        } else {
            val amount = quantity.removeSuffix(" (noted)").toInt()
            builder.addDrop(Builder.Drop(id, amount..amount, chance, roll, members))
        }
    }

    class Builder() {
        var name: String = ""
        var type: TableType = TableType.First
        var roll: Int = 1
        val drops = mutableListOf<Drop>()

        constructor(all: MutableList<Builder>) : this() {
            val roll = all.maxOf { it.roll }
            withRoll(roll)
            for (child in all) {
                if (child.roll == 1) {
                    continue
                }
                withType(child.type)
                val multiplier = roll / child.roll
                for (drop in child.drops) {
                    addDrop(drop.copy(chance = drop.chance * multiplier))
                }
            }
        }

        fun build() {
            val roll = drops.maxOf { it.roll }
            withRoll(roll)
            for (drop in drops) {
                val multiplier = roll / drop.roll
                drop.chance *= multiplier
            }
        }

        data class Drop(
            val id: String,
            val amount: IntRange = 1..1,
            var chance: Int = 1,
            val roll: Int = 1,
            val members: Boolean? = null,
        ) {
            override fun toString(): String {
                return Config.stringWriter {
                    write("  { ")
                    val table = amount == -1..-1
                    writeKey(if (table) "table" else "id")
                    writeValue(id)
                    if (!table && amount != 1..1) {
                        write(", ")
                        if (amount.first == amount.last) {
                            writeKey("amount")
                            writeValue(amount.first)
                        } else {
                            writeKey("min")
                            writeValue(amount.first)
                            write(", ")
                            writeKey("max")
                            writeValue(amount.last)
                        }
                    }
                    if (chance != 1) {
                        write(", ")
                        writeKey("chance")
                        writeValue(chance)
                    }
                    if (members != null) {
                        write(", ")
                        writeKey("members")
                        writeValue(members)
                    }
                    write(" },\n")
                }
            }

            companion object {
                fun table(name: String, chance: Int = 1, roll: Int = 1, members: Boolean? = null) = Drop(
                    name,
                    -1..-1,
                    chance,
                    roll,
                    members,
                )
            }
        }

        fun addDrop(drop: Drop): Builder {
            this.drops.add(drop)
            return this
        }

        fun withRoll(total: Int): Builder {
            this.roll = total
            return this
        }

        fun withType(type: TableType): Builder {
            this.type = type
            return this
        }

        override fun toString() = Config.stringWriter {
            writeSection(name)
            if (type == TableType.All) {
                writePair("type", "all")
            }
            if (roll != 1) {
                writePair("roll", roll)
            }
            writeKey("drops")
            write("[\n")
            for (drop in drops) {
                write(drop.toString())
            }
            write("]\n")
        }
    }
}
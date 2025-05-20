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
===Tertiary===
{{DropsTableHead|dropversion=Regular}}
{{DropsLine|name=Looting bag|namenotes={{(m)}}|quantity=1|rarity=1/3|raritynotes=<ref group="d">Looting bags are only dropped by those found in the [[Wilderness]].</ref>|gemw=No|leagueRegion=Wilderness}}
{{DropsLine|name=Moss giant bone|namenotes={{(m)}}|quantity=1|rarity=1/4|raritynotes=<ref group=d>Moss giant bones are only dropped during [[Rag and Bone Man II]].</ref>|gemw=No}}
{{DropsLine|name=Ensouled giant head|namenotes={{(m)}}|quantity=1|rarity=1/24}}
{{DropsLine|name=Clue scroll (beginner)|quantity=1|rarity=1/45|gemw=No}}
{{DropsLine|name=Long bone|namenotes={{(m)}}|quantity=1|rarity=1/400}}
{{DropsLine|name=Giant champion scroll|namenotes={{(m)}}|quantity=1|rarity=1/5000|gemw=No}}
{{DropsLine|name=Curved bone|namenotes={{(m)}}|quantity=1|rarity=1/5012.5}}
{{DropsTableBottom}}
        """.trimIndent()
        val npc = "moss_giant"
        val all = mutableListOf<Builder>()
        var builder = Builder()
        for (line in string.lines()) {
            if (line.startsWith("=")) {
                val name = toIdentifier(line.replace("=", "").trim())
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
                builder.addDrop(Builder.Drop.table("${name}_drop_table", chance = chance, roll = roll))
                builder.withRoll(roll)
                all.add(builder)
                builder = Builder()
            } else if (line.startsWith("{{GemDropTable")) {
                val parts = line.trim('{', ' ', '}').split('|')
                val split = parts[1].split("/")
                val chance = split[0].toInt()
                val roll = split[1].toInt()
                builder.addDrop(Builder.Drop.table("gem_drop_table", chance = chance, roll = roll))
                builder.withRoll(roll)
                all.add(builder)
                builder = Builder()
            } else if (line.startsWith("{{RareDropTable")) {
                val parts = line.trim('{', ' ', '}').split('|')
                val split = parts[1].split("/")
                val chance = split[0].toInt()
                val roll = split[1].toInt()
                builder.addDrop(Builder.Drop.table("rare_drop_table", chance = chance, roll = roll))
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
        parent.type = TableType.All
        queue.add(parent)
        val always = all.firstOrNull { it.name == "100%" }
        if (always != null) {
            always.name = "${npc}_primary"
            all.remove(always)
            if (always.drops.all { it.id == "bones" }) {
                parent.addDrop(Builder.Drop("bones"))
            } else if (always.drops.all { it.id == "big_bones" }) {
                parent.addDrop(Builder.Drop("big_bones"))
            } else {
                queue.add(always)
                parent.addDrop(Builder.Drop.table(always.name))
            }
        }
        val tertiary = all.firstOrNull { it.name == "tertiary" }
        if (tertiary != null) {
            all.remove(tertiary)
        }
        if (all.isNotEmpty()) {
            val combined = Builder(all)
            combined.name = "${npc}_secondary"
            parent.addDrop(Builder.Drop.table(combined.name))
            queue.add(combined)
        }
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
            val key = parts[i].lowercase()
            if (!map.containsKey(key)) {
                map[key] = parts[i + 1].removeSuffix("}}").removePrefix("{{")
            }
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
            rarity.split("/").map { if (it.contains(".")) it.toDouble().toInt() else it.toInt() }
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
                "Brimstone rarity" -> return
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
            val (low, high) = quantity.removeSuffix(" (noted)").split("-")
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
                drops.addAll(child.drops)
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
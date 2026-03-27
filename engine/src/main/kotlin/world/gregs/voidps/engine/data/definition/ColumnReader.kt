package world.gregs.voidps.engine.data.definition

import world.gregs.config.ConfigReader
import world.gregs.voidps.engine.entity.character.player.skill.Skill

sealed interface ColumnReader<T : Any> {
    val type: ColumnType<T>
    fun list(): MutableList<T>
    fun read(reader: ConfigReader): T

    object ReaderBoolean : ColumnReader<Boolean> {
        override val type = ColumnType.ColumnBoolean
        override fun list() = mutableListOf<Boolean>()
        override fun read(reader: ConfigReader) = reader.boolean()
    }

    object ReaderInt : ColumnReader<Int> {
        override val type = ColumnType.ColumnInt
        override fun list() = mutableListOf<Int>()
        override fun read(reader: ConfigReader) = reader.int()
    }

    object ReaderIntRange : ColumnReader<IntRange> {
        override val type = ColumnType.ColumnIntRange
        override fun list() = mutableListOf<IntRange>()
        override fun read(reader: ConfigReader): IntRange {
            reader.nextElement()
            val one = reader.int()
            reader.nextElement()
            val two = reader.int()
            reader.nextElement()
            return one..two
        }
    }

    class ReaderEntity(val ids: Map<String, Int>) : ColumnReader<Int> {
        override val type = ColumnType.ColumnEntity
        override fun list() = mutableListOf<Int>()
        override fun read(reader: ConfigReader) = ids.getValue(reader.string())
    }

    object ReaderString : ColumnReader<String> {
        override val type = ColumnType.ColumnString
        override fun list() = mutableListOf<String>()
        override fun read(reader: ConfigReader) = reader.string()
    }

    data class ReaderPair<A : Any, B : Any>(val one: ColumnReader<A>, val two: ColumnReader<B>) : ColumnReader<Pair<A, B>> {
        override val type = ColumnType.ColumnPair(one.type, two.type)
        override fun list() = mutableListOf<Pair<A, B>>()
        override fun read(reader: ConfigReader): Pair<A, B> {
            reader.nextElement()
            val one = one.read(reader)
            reader.nextElement()
            val two = two.read(reader)
            reader.nextElement()
            return Pair(one, two)
        }
    }

    data class ReaderTriple<A : Any, B : Any, C : Any>(val one: ColumnReader<A>, val two: ColumnReader<B>, val three: ColumnReader<C>) : ColumnReader<Triple<A, B, C>> {
        override val type = ColumnType.ColumnTriple(one.type, two.type, three.type)
        override fun list() = mutableListOf<Triple<A, B, C>>()
        override fun read(reader: ConfigReader): Triple<A, B, C> {
            reader.nextElement()
            val one = one.read(reader)
            reader.nextElement()
            val two = two.read(reader)
            reader.nextElement()
            val three = three.read(reader)
            reader.nextElement()
            return Triple(one, two, three)
        }
    }

    data class ReaderList<T : Any>(val read: ColumnReader<T>) : ColumnReader<List<T>> {
        override val type = ColumnType.ColumnList(read.type)
        override fun list() = mutableListOf<List<T>>()
        override fun read(reader: ConfigReader): List<T> {
            val list = read.list()
            while (reader.nextElement()) {
                list.add(read.read(reader))
            }
            return list
        }
    }

    companion object {
        fun reader(name: String): ColumnReader<*> = when (name) {
            "boolean" -> ReaderBoolean
            "int" -> ReaderInt
            "range" -> ReaderIntRange
            "string" -> ReaderString
            "skill" -> ReaderEntity(Skill.map)
            "npc" -> ReaderEntity(NPCDefinitions.ids)
            "item" -> ReaderEntity(ItemDefinitions.ids)
            "obj" -> ReaderEntity(ObjectDefinitions.ids)
            "row" -> ReaderString
            else -> if (name.startsWith("pair<", ignoreCase = true)) {
                val (first, second) = name.substringAfter("<").removeSuffix(">").split(",")
                ReaderPair(reader(first.trim()), reader(second.trim()))
            } else if (name.startsWith("triple<", ignoreCase = true)) {
                val (first, second, third) = name.substringAfter("<").removeSuffix(">").split(",")
                ReaderTriple(reader(first.trim()), reader(second.trim()), reader(third.trim()))
            } else if (name.startsWith("list<", ignoreCase = true) && name.endsWith(">")) {
                val type = name.substringAfter("<").substringBefore(">")
                ReaderList(reader(type))
            } else {
                throw IllegalArgumentException("Unsupported type '$name'")
            }
        }

    }
}

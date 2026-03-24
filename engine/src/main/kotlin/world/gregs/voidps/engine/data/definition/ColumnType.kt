@file:Suppress("UNCHECKED_CAST")

package world.gregs.voidps.engine.data.definition

import world.gregs.config.ConfigReader

sealed interface ColumnType<T : Any> {
    val default: T
    fun cast(value: Any?): T? = value as? T

    object ColumnBoolean : ColumnType<Boolean> {
        override val default = false
        override fun toString() = "ColumnBoolean"
    }

    object ColumnInt : ColumnType<Int> {
        override val default = 0
        override fun toString() = "ColumnInt"
    }

    object ColumnString : ColumnType<String> {
        override val default = ""
        override fun toString() = "ColumnString"
    }

    object ColumnEntity : ColumnType<Int> {
        override val default = -1
        override fun toString() = "ColumnEntity"
    }

    open class ColumnPair<A : Any, B : Any>(val one: ColumnType<A>, val two: ColumnType<B>) : ColumnType<Pair<A, B>> {
        override val default = Pair(one.default, two.default)
        override fun toString(): String {
            return "ColumnPair(one=$one, two=$two)"
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ColumnPair<*, *>

            if (one != other.one) return false
            if (two != other.two) return false

            return true
        }

        override fun hashCode(): Int {
            var result = one.hashCode()
            result = 31 * result + two.hashCode()
            return result
        }

    }

    open class ColumnTriple<A : Any, B : Any, C : Any>(val one: ColumnType<A>, val two: ColumnType<B>, val three: ColumnType<C>) : ColumnType<Triple<A, B, C>> {
        override val default = Triple(one.default, two.default, three.default)
        override fun toString(): String {
            return "ColumnTriple(one=$one, two=$two, three=$three)"
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ColumnTriple<*, *, *>

            if (one != other.one) return false
            if (two != other.two) return false
            if (three != other.three) return false

            return true
        }

        override fun hashCode(): Int {
            var result = one.hashCode()
            result = 31 * result + two.hashCode()
            result = 31 * result + three.hashCode()
            return result
        }

    }

    open class ColumnList<T : Any>(val type: ColumnType<T>) : ColumnType<List<T>> {
        override val default = emptyList<T>()
        override fun toString(): String {
            return "ColumnList(type=$type)"
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ColumnList<*>

            return type == other.type
        }

        override fun hashCode(): Int {
            return type.hashCode()
        }

    }

    object BooleanList : ColumnList<Boolean>(ColumnBoolean)
    object IntList : ColumnList<Int>(ColumnInt)
    object StringList : ColumnList<String>(ColumnString)

    object IntIntPair : ColumnPair<Int, Int>(ColumnInt, ColumnInt)
    object IntStringPair : ColumnPair<Int, String>(ColumnInt, ColumnString)
    object StringIntPair : ColumnPair<String, Int>(ColumnString, ColumnInt)

    object IntIntList : ColumnList<Pair<Int, Int>>(ColumnPair(ColumnInt, ColumnInt))
    object IntStringList : ColumnList<Pair<Int, String>>(ColumnPair(ColumnInt, ColumnString))
    object StringIntList : ColumnList<Pair<String, Int>>(ColumnPair(ColumnString, ColumnInt))
}

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

    class ReaderEntity(val definitions: Map<String, Int>) : ColumnReader<Int> {
        override val type = ColumnType.ColumnEntity
        override fun list() = mutableListOf<Int>()
        override fun read(reader: ConfigReader) = definitions.getValue(reader.string())
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
            "Boolean" -> ReaderBoolean
            "Int" -> ReaderInt
            "String" -> ReaderString
            "NPC" -> ReaderEntity(NPCDefinitions.ids)
            "Item" -> ReaderEntity(ItemDefinitions.ids)
            "GameObject" -> ReaderEntity(ObjectDefinitions.ids)
            "Row" -> ReaderEntity(Rows.ids)
            else -> if (name.startsWith("Pair<")) {
                val (first, second) = name.substringAfter("<").removeSuffix(">").split(",")
                ReaderPair(reader(first.trim()), reader(second.trim()))
            } else if (name.startsWith("Triple<")) {
                val (first, second, third) = name.substringAfter("<").removeSuffix(">").split(",")
                ReaderTriple(reader(first.trim()), reader(second.trim()), reader(third.trim()))
            } else if (name.startsWith("List<") && name.endsWith(">")) {
                val type = name.substringAfter("<").substringBefore(">")
                ReaderList(reader(type))
            } else {
                throw IllegalArgumentException("Unsupported type '$name'")
            }
        }

    }
}

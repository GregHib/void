@file:Suppress("UNCHECKED_CAST")

package world.gregs.voidps.engine.data.definition

import world.gregs.config.ConfigReader

sealed interface ColumnType<Access : Any, Encoded : Any> {
    val size: Int
    val default: Encoded
    fun cast(value: Any): Access?
    fun default(defaults: Array<Any?>, index: Int): Access?
    fun encode(value: Access): Encoded
    fun decode(value: Encoded): Access
    val defaultValue: Access
        get() = decode(default)

    fun default(index: Int): Any = default

    fun read(row: Array<Any?>, index: Int): Access?

    fun add(row: MutableList<Any?>, reader: ConfigReader)

    fun set(row: Array<Any?>, index: Int, reader: ConfigReader)

    sealed class SingleRowType<T : Any> : ColumnType<T, T> {
        override val size = 1
        override fun encode(value: T) = value
        override fun decode(value: T) = value
        abstract fun read(reader: ConfigReader): T

        override fun add(row: MutableList<Any?>, reader: ConfigReader) {
            row.add(encode(read(reader)))
        }

        override fun set(row: Array<Any?>, index: Int, reader: ConfigReader) {
            val value = read(reader)
            println("Set $index $value")
            row[index] = encode(value)
        }

        override fun read(row: Array<Any?>, index: Int): T? {
            return cast(row[index] ?: return null)
        }

        override fun default(defaults: Array<Any?>, index: Int): T? {
            val default = defaults.getOrNull(index) ?: return null
            return cast(default)
        }
    }

    sealed class IntEncodedString : ColumnType<String, Int> {
        override val size = 1
        override val default = -1

        override fun cast(value: Any): String? = (value as? Int)?.let { decode(it) }

        override fun set(row: Array<Any?>, index: Int, reader: ConfigReader) {
            row[index] = encode(reader.string())
        }

        override fun add(row: MutableList<Any?>, reader: ConfigReader) {
            row.add(encode(reader.string()))
        }

        override fun read(row: Array<Any?>, index: Int): String? {
            return cast(row[index] ?: return null)
        }

        override fun default(defaults: Array<Any?>, index: Int): String? {
            val default = defaults.getOrNull(index) ?: return null
            return cast(default)
        }
    }

    object BooleanType : SingleRowType<Boolean>() {
        override val default = false
        override fun read(reader: ConfigReader) = reader.boolean()
        override fun cast(value: Any) = value as? Boolean
        override fun toString() = "BooleanType"
    }

    object IntType : SingleRowType<Int>() {
        override val default = 0
        override fun read(reader: ConfigReader) = reader.int()
        override fun cast(value: Any) = value as? Int
        override fun toString() = "IntType"
    }

    object StringType : SingleRowType<String>() {
        override val default = ""
        override fun read(reader: ConfigReader) = reader.string()
        override fun cast(value: Any) = value as? String
        override fun toString() = "StringType"
    }

    object ItemType : IntEncodedString() {
        override fun encode(value: String): Int = ItemDefinitions.getOrNull(value)?.id ?: error("Unknown item: $value")
        override fun decode(value: Int): String = ItemDefinitions.getOrNull(value)?.stringId ?: error("Unknown item: $value")
        override fun toString() = "ItemType"
    }

    object ObjectType : IntEncodedString() {
        override fun encode(value: String): Int = ObjectDefinitions.getOrNull(value)?.id ?: error("Unknown object: $value")
        override fun decode(value: Int): String = ObjectDefinitions.getOrNull(value)?.stringId ?: error("Unknown object: $value")
        override fun toString() = "ObjectType"
    }

    object NPCType : IntEncodedString() {
        override fun encode(value: String): Int = NPCDefinitions.getOrNull(value)?.id ?: error("Unknown object: $value")
        override fun decode(value: Int): String = NPCDefinitions.getOrNull(value)?.stringId ?: error("Unknown object: $value")
        override fun toString() = "NPCType"
    }

    object RowType : IntEncodedString() {
        override fun encode(value: String): Int = Rows.ids[value] ?: error("Unknown table row: $value")
        override fun decode(value: Int): String = Rows.getOrNull(value)?.stringId ?: error("Unknown table row: $value")
        override fun toString() = "RowType"
    }

    object IntList : RowList<Int>(IntType)
    object StringList : RowList<String>(StringType)
    object ItemList : RowList<String>(ItemType)
    object NPCList : RowList<String>(NPCType)
    object ObjectList : RowList<String>(ObjectType)
    object IntIntPair : RowPair<Int, Int>(IntType, IntType)
    object IntStringPair : RowPair<Int, String>(IntType, StringType)
    object StringIntPair : RowPair<String, Int>(StringType, IntType)
    object IntIntList : RowList<Pair<Int, Int>>(RowPair(IntType, IntType))
    object IntStringList : RowList<Pair<Int, String>>(RowPair(IntType, StringType))
    object StringIntList : RowList<Pair<String, Int>>(RowPair(StringType, IntType))

    open class RowPair<A : Any, B : Any>(val one: ColumnType<A, *>, val two: ColumnType<B, *>) : SingleRowType<Pair<A, B>>() {
        override val default = Pair(one.defaultValue, two.defaultValue)
        override val size: Int = 2

        override fun default(index: Int): Any {
            return if (index == 0) one.default(index) else two.default(index + 1)
        }

        override fun read(reader: ConfigReader): Pair<A, B> {
            throw NotImplementedError("Shouldn't be called")
        }

        override fun cast(value: Any): Pair<A, B>? {
            return value as? Pair<A, B>
        }

        override fun default(defaults: Array<Any?>, index: Int): Pair<A, B>? {
            val first = one.default(defaults, index) ?: return null
            val second = two.default(defaults, index + one.size) ?: return null
            return Pair(first, second)
        }

        override fun read(row: Array<Any?>, index: Int): Pair<A, B>? {
            val first = one.read(row, index) ?: return null
            val second = two.read(row, index + one.size) ?: return null
            return Pair(first, second)
        }

        override fun add(row: MutableList<Any?>, reader: ConfigReader) {
            one.add(row, reader)
            two.add(row, reader)
        }

        override fun set(row: Array<Any?>, index: Int, reader: ConfigReader) {
            var count = 0
            while (reader.nextElement()) {
                when (count) {
                    0 -> one.set(row, index, reader)
                    1 -> two.set(row, index + one.size, reader)
                    else -> throw IllegalArgumentException("Unexpected pair index: $count")
                }
                count++
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as RowPair<*, *>

            if (one != other.one) return false
            if (two != other.two) return false

            return true
        }

        override fun hashCode(): Int {
            var result = one.hashCode()
            result = 31 * result + two.hashCode()
            return result
        }

        override fun toString(): String {
            return "RowPair($one, $two)"
        }
    }

    open class RowTriple<A : Any, B : Any, C: Any>(val one: ColumnType<A, *>, val two: ColumnType<B, *>, val three: ColumnType<C, *>) : SingleRowType<Triple<A, B, C>>() {
        override val default = Triple(one.defaultValue, two.defaultValue, three.defaultValue)
        override val size: Int = 2

        override fun default(index: Int): Any {
            return when (index) {
                0 -> one.default(index)
                1 -> two.default(index + one.size)
                2 -> three.default(index + one.size + two.size)
                else -> throw IllegalArgumentException("Unexpected pair index: $index")
            }
        }

        override fun read(reader: ConfigReader): Triple<A, B, C> {
            throw NotImplementedError("Shouldn't be called")
        }

        override fun cast(value: Any): Triple<A, B, C>? {
            return value as? Triple<A, B, C>
        }

        override fun default(defaults: Array<Any?>, index: Int): Triple<A, B, C>? {
            val first = one.default(defaults, index) ?: return null
            val second = two.default(defaults, index + one.size) ?: return null
            val third = three.default(defaults, index + one.size + two.size) ?: return null
            return Triple(first, second, third)
        }

        override fun read(row: Array<Any?>, index: Int): Triple<A, B, C>? {
            val first = one.read(row, index) ?: return null
            val second = two.read(row, index + one.size) ?: return null
            val third = three.read(row, index + one.size + two.size) ?: return null
            return Triple(first, second, third)
        }

        override fun add(row: MutableList<Any?>, reader: ConfigReader) {
            one.add(row, reader)
            two.add(row, reader)
            three.add(row, reader)
        }

        override fun set(row: Array<Any?>, index: Int, reader: ConfigReader) {
            var index = 0
            while (reader.nextElement()) {
                when (index) {
                    0 -> one.set(row, index, reader)
                    1 -> two.set(row, index + one.size, reader)
                    2 -> three.set(row, index + one.size + two.size, reader)
                    else -> throw IllegalArgumentException("Unexpected pair index: $index")
                }
                index++
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as RowTriple<*, *, *>

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

        override fun toString(): String {
            return "RowTriple($one, $two, $three)"
        }

    }

    open class RowList<T : Any>(
        val type: ColumnType<T, *>,
        size: Int = 0
    ) : SingleRowType<List<T>>() {
        override val default = emptyList<T>()
        override val size: Int = (size * type.size) + 1

        override fun read(reader: ConfigReader): List<T> {
            throw NotImplementedError("Shouldn't be called")
        }

        override fun cast(value: Any): List<T>? {
            return value as? List<T>
        }

        override fun default(defaults: Array<Any?>, index: Int): List<T>? {
            return read(defaults, index)
        }

        override fun read(row: Array<Any?>, index: Int): List<T>? {
            val size = IntType.read(row, index) ?: return null
            return List(size) { type.read(row, it + 1)!! }
        }

        override fun add(row: MutableList<Any?>, reader: ConfigReader) {
            val index = row.size
            row.add(0) // Placeholder
            var count = 0
            while (reader.nextElement()) {
                type.add(row, reader)
                count += type.size
            }
            row[index] = count
        }

        override fun set(row: Array<Any?>, index: Int, reader: ConfigReader) {
            var acc = 0
            var count = 0
            while (reader.nextElement()) {
                type.set(row, index + 1 + acc, reader)
                acc += type.size
                count++
            }
            println("Set size $index $count")
            row[index] = count
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as RowList<*>

            return type == other.type
        }

        override fun hashCode(): Int {
            return type.hashCode()
        }

        override fun toString(): String {
            return "RowList($type)"
        }

    }

    companion object {
        fun type(name: String): ColumnType<*, *> = when (name) {
            "Boolean" -> BooleanType
            "Int" -> IntType
            "String" -> StringType
            "NPC" -> NPCType
            "Item" -> ItemType
            "GameObject" -> ObjectType
            "Row" -> RowType
            else -> if (name.startsWith("Pair<")) {
                val (first, second) = name.substringAfter("<").removeSuffix(">").split(",")
                RowPair(type(first.trim()), type(second.trim()))
            } else if (name.startsWith("Triple<")) {
                val (first, second, third) = name.substringAfter("<").removeSuffix(">").split(",")
                RowTriple(type(first.trim()), type(second.trim()), type(third.trim()))
            } else if (name.startsWith("List<") && name.contains(">(")) {
                val type = name.substringAfter("<").substringBefore(">(")
                val size = name.substringAfter(">(").removeSuffix(")").trim().toInt()
                RowList(type(type), size)
            } else {
                throw IllegalArgumentException("Unsupported type '$name'")
            }
        }

    }
}
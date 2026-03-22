@file:Suppress("UNCHECKED_CAST")

package world.gregs.voidps.engine.data.definition

import world.gregs.config.ConfigReader

sealed interface ColumnType<Access : Any, Encoded : Any> {
    val default: Encoded
    fun read(reader: ConfigReader): Access
    fun readEncoded(reader: ConfigReader): Encoded = encode(read(reader))
    fun cast(value: Any): Access?
    fun encode(value: Access): Encoded
    fun decode(value: Encoded): Access
    val defaultValue: Access
        get() = decode(default)

    sealed class SingleRowType<T : Any> : ColumnType<T, T> {
        override fun encode(value: T) = value
        override fun decode(value: T) = value
    }

    object IntType : SingleRowType<Int>() {
        override val default = 0
        override fun read(reader: ConfigReader) = reader.int()
        override fun cast(value: Any) = value as? Int
    }

    object RowType : ColumnType<String, Int> {
        override val default = -1
        override fun read(reader: ConfigReader): String = reader.string()
        override fun encode(value: String): Int = Rows.ids[value] ?: error("Unknown table row: $value")
        override fun decode(value: Int): String = Rows.getOrNull(value)?.stringId ?: error("Unknown table row: $value")
        override fun cast(value: Any): String? = (value as? Int)?.let { decode(it) }
    }

    object StringType : SingleRowType<String>() {
        override val default = ""
        override fun read(reader: ConfigReader) = reader.string()
        override fun cast(value: Any) = value as? String
    }

    object ItemType : ColumnType<String, Int> {
        override val default = -1
        override fun read(reader: ConfigReader): String = reader.string()
        override fun encode(value: String): Int = ItemDefinitions.getOrNull(value)?.id ?: error("Unknown item: $value")
        override fun decode(value: Int): String = ItemDefinitions.getOrNull(value)?.stringId ?: error("Unknown item: $value")
        override fun cast(value: Any): String? = (value as? Int)?.let { decode(it) }
    }

    object ObjectType : ColumnType<String, Int> {
        override val default = -1
        override fun read(reader: ConfigReader): String = reader.string()
        override fun encode(value: String): Int = ObjectDefinitions.getOrNull(value)?.id ?: error("Unknown object: $value")
        override fun decode(value: Int): String = ObjectDefinitions.getOrNull(value)?.stringId ?: error("Unknown object: $value")
        override fun cast(value: Any): String? = (value as? Int)?.let { decode(it) }
    }

    object IntList : SingleRowType<List<Int>>() {
        override val default = emptyList<Int>()
        override fun read(reader: ConfigReader) = reader.readList(IntType)
        override fun cast(value: Any) = value as? List<Int>
    }

    object StringList : SingleRowType<List<String>>() {
        override val default = emptyList<String>()
        override fun read(reader: ConfigReader) = reader.readList(StringType)
        override fun cast(value: Any) = value as? List<String>
    }

    object ItemList : ColumnType<List<String>, IntArray> {
        override val default = IntArray(0)
        override fun read(reader: ConfigReader) = reader.readList(StringType)
        override fun cast(value: Any) = (value as? IntArray)?.let { decode(it) }
        override fun encode(value: List<String>) = value.map(ItemType::encode).toIntArray()
        override fun decode(value: IntArray) = value.map(ItemType::decode)
    }

    object ObjectList : ColumnType<List<String>, IntArray> {
        override val default = IntArray(0)
        override fun read(reader: ConfigReader) = reader.readList(StringType)
        override fun cast(value: Any) = (value as? IntArray)?.let { decode(it) }
        override fun encode(value: List<String>) = value.map(ObjectType::encode).toIntArray()
        override fun decode(value: IntArray) = value.map(ObjectType::decode)
    }

    object ItemIdType : SingleRowType<Int>() {
        override val default = -1
        override fun read(reader: ConfigReader): Int = ItemType.encode(reader.string())
        override fun cast(value: Any) = IntType.cast(value)
    }

    object ItemIdList : SingleRowType<IntArray>() {
        override val default = IntArray(0)
        override fun read(reader: ConfigReader) = reader.readList(IntType).toIntArray()
        override fun cast(value: Any) = value as? IntArray
    }

    object IntIntPair : SingleRowType<Pair<Int, Int>>() {
        override val default = Pair(IntType.default, IntType.default)
        override fun read(reader: ConfigReader) = reader.readPair(IntType, IntType)
        override fun cast(value: Any) = value as? Pair<Int, Int>
    }

    object StrIntPair : SingleRowType<Pair<String, Int>>() {
        override val default = Pair(StringType.default, IntType.default)
        override fun read(reader: ConfigReader) = reader.readPair(StringType, IntType)
        override fun cast(value: Any) = value as? Pair<String, Int>
    }

    object IntStrPair : SingleRowType<Pair<Int, String>>() {
        override val default = Pair(IntType.default, StringType.default)
        override fun read(reader: ConfigReader) = reader.readPair(IntType, StringType)
        override fun cast(value: Any) = value as? Pair<Int, String>
    }

    object IntIntList : SingleRowType<List<Pair<Int, Int>>>() {
        override val default = emptyList<Pair<Int, Int>>()
        override fun read(reader: ConfigReader) = reader.readList(IntIntPair)
        override fun cast(value: Any) = value as? List<Pair<Int, Int>>
    }

    object IntStrList : SingleRowType<List<Pair<Int, String>>>() {
        override val default = emptyList<Pair<Int, String>>()
        override fun read(reader: ConfigReader) = reader.readList(IntStrPair)
        override fun cast(value: Any) = value as? List<Pair<Int, String>>
    }

    object StrIntList : SingleRowType<List<Pair<String, Int>>>() {
        override val default = emptyList<Pair<String, Int>>()
        override fun read(reader: ConfigReader) = reader.readList(StrIntPair)
        override fun cast(value: Any) = value as? List<Pair<String, Int>>
    }

    companion object {
        fun type(name: String): ColumnType<*, *> = when (name.lowercase()) {
            "int" -> IntType
            "string" -> StringType
            "item" -> ItemType
            "gameobject" -> ObjectType
            "row" -> RowType
            "list<int>" -> IntList
            "list<string>" -> StringList
            "list<item>" -> ItemList
            "list<gameobject>" -> ObjectList
            "pair<int, int>" -> IntIntPair
            "pair<string, int>" -> StrIntPair
            "pair<int, string>" -> IntStrPair
            "list<pair<int, int>>" -> IntIntList
            "list<pair<string, int>>" -> StrIntList
            "list<pair<int, string>>" -> IntStrList
            else -> throw IllegalArgumentException("Unsupported type $name")
        }

        private fun <A : Any, B : Any> ConfigReader.readPair(one: ColumnType<A, *>, two: ColumnType<B, *>): Pair<A, B> {
            var index = 0
            var a = one.defaultValue
            var b = two.defaultValue
            while (nextElement()) {
                when (index++) {
                    0 -> a = one.read(this)
                    1 -> b = two.read(this)
                    else -> throw IllegalArgumentException("Unexpected pair index: $index")
                }
            }
            return Pair(a, b)
        }

        private fun <T : Any> ConfigReader.readList(type: ColumnType<T, *>): List<T> {
            val list = mutableListOf<T>()
            while (nextElement()) {
                list.add(type.read(this))
            }
            return list
        }

    }
}
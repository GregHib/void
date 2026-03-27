@file:Suppress("UNCHECKED_CAST")

package world.gregs.voidps.engine.data.definition

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

    object ColumnIntRange : ColumnType<IntRange> {
        override val default = 0..0
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


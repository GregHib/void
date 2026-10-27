package world.gregs.voidps.engine.data.snapshot

import world.gregs.voidps.engine.entity.character.player.equip.EquipType
import world.gregs.voidps.engine.entity.item.ItemKept
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Area
import world.gregs.voidps.type.area.Cuboid
import world.gregs.voidps.type.area.Polygon
import world.gregs.voidps.type.area.Rectangle

class SnapshotWriter {
    private var buffer = ByteArray(1 shl 16)
    private var position = 0

    fun toByteArray(): ByteArray = buffer.copyOf(position)

    private fun ensure(size: Int) {
        if (position + size > buffer.size) {
            buffer = buffer.copyOf(maxOf(buffer.size * 2, position + size))
        }
    }

    fun byte(value: Int) {
        ensure(1)
        buffer[position++] = value.toByte()
    }

    fun boolean(value: Boolean) = byte(if (value) 1 else 0)

    fun int(value: Int) {
        ensure(4)
        buffer[position++] = (value ushr 24).toByte()
        buffer[position++] = (value ushr 16).toByte()
        buffer[position++] = (value ushr 8).toByte()
        buffer[position++] = value.toByte()
    }

    fun long(value: Long) {
        int((value ushr 32).toInt())
        int(value.toInt())
    }

    fun double(value: Double) = long(java.lang.Double.doubleToRawLongBits(value))

    fun string(value: String) {
        val bytes = value.toByteArray(Charsets.UTF_8)
        int(bytes.size)
        ensure(bytes.size)
        System.arraycopy(bytes, 0, buffer, position, bytes.size)
        position += bytes.size
    }

    fun nullableString(value: String?) {
        boolean(value != null)
        if (value != null) {
            string(value)
        }
    }

    fun nullableInt(value: Int?) {
        boolean(value != null)
        if (value != null) {
            int(value)
        }
    }

    fun <T> list(values: List<T>, write: (T) -> Unit) {
        int(values.size)
        for (value in values) {
            write(value)
        }
    }

    fun nullableStrings(values: Array<String?>?) {
        if (values == null) {
            int(-1)
            return
        }
        int(values.size)
        for (value in values) {
            nullableString(value)
        }
    }

    fun nullableInts(values: IntArray?) {
        if (values == null) {
            int(-1)
            return
        }
        ints(values)
    }

    fun bytes(values: ByteArray) {
        int(values.size)
        ensure(values.size)
        System.arraycopy(values, 0, buffer, position, values.size)
        position += values.size
    }

    fun nullableShorts(values: ShortArray?) {
        if (values == null) {
            int(-1)
            return
        }
        int(values.size)
        ensure(values.size * 2)
        for (value in values) {
            buffer[position++] = (value.toInt() ushr 8).toByte()
            buffer[position++] = value.toByte()
        }
    }

    fun nullableByteArrays(values: Array<ByteArray>?) {
        if (values == null) {
            int(-1)
            return
        }
        int(values.size)
        for (value in values) {
            bytes(value)
        }
    }

    fun nullableIntArrays(values: Array<IntArray>?) {
        if (values == null) {
            int(-1)
            return
        }
        int(values.size)
        for (value in values) {
            ints(value)
        }
    }

    fun nullableStringArray(values: Array<String>?) {
        if (values == null) {
            int(-1)
            return
        }
        int(values.size)
        for (value in values) {
            string(value)
        }
    }

    fun nullableAnyArray(values: Array<Any>?) {
        if (values == null) {
            int(-1)
            return
        }
        int(values.size)
        for (value in values) {
            value(value)
        }
    }

    /** String ids with their numeric id */
    fun ids(ids: Map<String, Int>) {
        int(ids.size)
        for ((stringId, id) in ids) {
            string(stringId)
            int(id)
        }
    }

    fun ints(values: IntArray) {
        int(values.size)
        for (value in values) {
            int(value)
        }
    }

    /**
     * Writes a param map in the form it ended up in after loading.
     */
    fun params(params: Map<Int, Any>?) {
        if (params == null) {
            int(-1)
            return
        }
        int(params.size)
        for ((key, value) in params) {
            int(key)
            value(value)
        }
    }

    fun area(area: Area) {
        when {
            area is Rectangle -> {
                byte(0)
                int(area.minX)
                int(area.minY)
                int(area.maxX)
                int(area.maxY)
            }
            area is Cuboid -> {
                byte(1)
                int(area.minX)
                int(area.minY)
                int(area.maxX)
                int(area.maxY)
                int(area.minLevel)
                int(area.maxLevel)
            }
            area.javaClass == Polygon::class.java -> {
                area as Polygon
                byte(2)
                ints(area.xPoints)
                ints(area.yPoints)
                int(area.level)
                int(area.bounds.maxLevel)
            }
            else -> throw IllegalArgumentException("Unsupported area type ${area.javaClass}")
        }
    }

    fun value(value: Any?) {
        when (value) {
            null -> byte(NULL)
            is Int -> {
                byte(INT)
                int(value)
            }
            is Long -> {
                byte(LONG)
                long(value)
            }
            is Double -> {
                byte(DOUBLE)
                double(value)
            }
            is Boolean -> {
                byte(BOOLEAN)
                boolean(value)
            }
            is String -> {
                byte(STRING)
                string(value)
            }
            is IntRange -> {
                byte(INT_RANGE)
                int(value.first)
                int(value.last)
            }
            is EquipSlot -> {
                byte(EQUIP_SLOT)
                string(value.name)
            }
            is EquipType -> {
                byte(EQUIP_TYPE)
                string(value.name)
            }
            is ItemKept -> {
                byte(ITEM_KEPT)
                string(value.name)
            }
            is Array<*> -> {
                require(value.javaClass.componentType == String::class.java) { "Unsupported snapshot array type ${value.javaClass}" }
                byte(STRING_ARRAY)
                int(value.size)
                for (element in value) {
                    nullableString(element as String?)
                }
            }
            is List<*> -> {
                byte(LIST)
                int(value.size)
                for (element in value) {
                    value(element)
                }
            }
            is Set<*> -> {
                byte(SET)
                int(value.size)
                for (element in value) {
                    value(element)
                }
            }
            is Map<*, *> -> {
                byte(MAP)
                int(value.size)
                for ((k, v) in value) {
                    string(k as? String ?: throw IllegalArgumentException("Unsupported snapshot map key ${k?.javaClass}"))
                    value(v)
                }
            }
            is Pair<*, *> -> {
                byte(PAIR)
                value(value.first)
                value(value.second)
            }
            is Triple<*, *, *> -> {
                byte(TRIPLE)
                value(value.first)
                value(value.second)
                value(value.third)
            }
            else -> throw IllegalArgumentException("Unsupported snapshot value type ${value.javaClass}")
        }
    }

    companion object {
        internal const val NULL = 0
        internal const val INT = 1
        internal const val LONG = 2
        internal const val DOUBLE = 3
        internal const val BOOLEAN = 4
        internal const val STRING = 5
        internal const val INT_RANGE = 6
        internal const val EQUIP_SLOT = 7
        internal const val EQUIP_TYPE = 8
        internal const val ITEM_KEPT = 9
        internal const val LIST = 10
        internal const val SET = 11
        internal const val MAP = 12
        internal const val PAIR = 13
        internal const val TRIPLE = 14
        internal const val STRING_ARRAY = 15
    }
}

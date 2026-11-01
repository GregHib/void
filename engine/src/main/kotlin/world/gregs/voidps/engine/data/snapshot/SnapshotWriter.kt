package world.gregs.voidps.engine.data.snapshot

import world.gregs.voidps.engine.entity.character.player.equip.EquipType
import world.gregs.voidps.engine.entity.item.ItemKept
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Direction

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

    fun short(value: Int) {
        ensure(2)
        buffer[position++] = (value ushr 8).toByte()
        buffer[position++] = value.toByte()
    }

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
            is Direction -> {
                byte(DIRECTION)
                int(value.ordinal)
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
        internal const val DIRECTION = 16
    }
}

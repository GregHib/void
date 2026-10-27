package world.gregs.voidps.engine.data.snapshot

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import world.gregs.voidps.engine.entity.character.player.equip.EquipType
import world.gregs.voidps.engine.entity.item.ItemKept
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import java.nio.ByteBuffer

class SnapshotReader(bytes: ByteArray) {
    private val array = bytes
    private val buffer = ByteBuffer.wrap(bytes)

    fun byte(): Int = buffer.get().toInt()

    fun boolean(): Boolean = buffer.get().toInt() != 0

    fun int(): Int = buffer.getInt()

    fun long(): Long = buffer.getLong()

    fun double(): Double = java.lang.Double.longBitsToDouble(long())

    fun string(): String {
        val length = int()
        val start = buffer.position()
        buffer.position(start + length)
        return String(array, start, length, Charsets.UTF_8)
    }

    fun nullableString(): String? = if (boolean()) string() else null

    fun ints(): IntArray = IntArray(int()) { int() }

    fun short(): Int = buffer.getShort().toInt()

    fun bytes(): ByteArray {
        val length = int()
        val start = buffer.position()
        buffer.position(start + length)
        return array.copyOfRange(start, start + length)
    }

    fun nullableShorts(): ShortArray? {
        val size = int()
        return if (size == -1) null else ShortArray(size) { buffer.getShort() }
    }

    fun nullableByteArrays(): Array<ByteArray>? {
        val size = int()
        return if (size == -1) null else Array(size) { bytes() }
    }

    fun nullableIntArrays(): Array<IntArray>? {
        val size = int()
        return if (size == -1) null else Array(size) { ints() }
    }

    fun nullableStringArray(): Array<String>? {
        val size = int()
        return if (size == -1) null else Array(size) { string() }
    }

    fun nullableAnyArray(): Array<Any>? {
        val size = int()
        return if (size == -1) null else Array(size) { value()!! }
    }

    /** Reads [ids] written by the writer, the same as the loaders would have built */
    fun ids(default: Int? = null): Object2IntOpenHashMap<String> {
        val size = int()
        val ids = Object2IntOpenHashMap<String>(size, Hash.VERY_FAST_LOAD_FACTOR)
        if (default != null) {
            ids.defaultReturnValue(default)
        }
        repeat(size) {
            val stringId = string()
            ids[stringId] = int()
        }
        return ids
    }

    fun nullableInt(): Int? = if (boolean()) int() else null

    fun <T> list(read: () -> T): MutableList<T> {
        val size = int()
        val list = ArrayList<T>(size)
        repeat(size) { list.add(read()) }
        return list
    }

    fun nullableInts(): IntArray? {
        val size = int()
        return if (size == -1) null else IntArray(size) { int() }
    }

    fun nullableStrings(): Array<String?>? {
        val size = int()
        return if (size == -1) null else Array(size) { nullableString() }
    }

    fun params(): Int2ObjectOpenHashMap<Any>? {
        val size = int()
        if (size == -1) {
            return null
        }
        val params = Int2ObjectOpenHashMap<Any>(maxOf(size, 4), Hash.VERY_FAST_LOAD_FACTOR)
        repeat(size) {
            val key = int()
            params[key] = value()!!
        }
        return params
    }

    fun value(): Any? = when (val tag = byte()) {
        SnapshotWriter.NULL -> null
        SnapshotWriter.INT -> int()
        SnapshotWriter.LONG -> long()
        SnapshotWriter.DOUBLE -> double()
        SnapshotWriter.BOOLEAN -> boolean()
        SnapshotWriter.STRING -> string()
        SnapshotWriter.INT_RANGE -> int()..int()
        SnapshotWriter.EQUIP_SLOT -> EquipSlot.valueOf(string())
        SnapshotWriter.EQUIP_TYPE -> EquipType.valueOf(string())
        SnapshotWriter.ITEM_KEPT -> ItemKept.valueOf(string())
        SnapshotWriter.LIST -> {
            val size = int()
            val list = ObjectArrayList<Any?>(size)
            repeat(size) { list.add(value()) }
            list
        }
        SnapshotWriter.SET -> {
            val size = int()
            val set = ObjectLinkedOpenHashSet<Any?>(size, Hash.VERY_FAST_LOAD_FACTOR)
            repeat(size) { set.add(value()) }
            set
        }
        SnapshotWriter.MAP -> {
            val size = int()
            val map = Object2ObjectOpenHashMap<String, Any?>(size, Hash.VERY_FAST_LOAD_FACTOR)
            repeat(size) {
                val key = string()
                map[key] = value()
            }
            map
        }
        SnapshotWriter.PAIR -> Pair(value(), value())
        SnapshotWriter.TRIPLE -> Triple(value(), value(), value())
        SnapshotWriter.STRING_ARRAY -> Array(int()) { nullableString() }
        else -> throw IllegalStateException("Unknown snapshot value tag $tag")
    }
}

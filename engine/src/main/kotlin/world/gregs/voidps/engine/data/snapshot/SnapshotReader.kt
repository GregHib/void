package world.gregs.voidps.engine.data.snapshot

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import world.gregs.voidps.engine.entity.character.player.equip.EquipType
import world.gregs.voidps.engine.entity.item.ItemKept
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Direction
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

    fun short(): Int = buffer.getShort().toInt()

    fun value(): Any? = when (val tag = byte()) {
        SnapshotWriter.NULL -> null
        SnapshotWriter.INT -> int()
        SnapshotWriter.LONG -> long()
        SnapshotWriter.DOUBLE -> double()
        SnapshotWriter.BOOLEAN -> boolean()
        SnapshotWriter.STRING -> string()
        SnapshotWriter.DIRECTION -> Direction.entries[int()]
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

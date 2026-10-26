package world.gregs.voidps.engine.data

import com.github.michaelbull.logging.InlineLogger
import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import world.gregs.voidps.engine.entity.character.player.equip.EquipType
import world.gregs.voidps.engine.entity.item.ItemKept
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.createParentDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes

/**
 * A binary copy of the final result of parsing a set of config files, so the files only need parsing again when they change.
 * Valid while the [key] (derived from the files' sizes and modified times) matches the one the snapshot was written with.
 */
class Snapshot(private val path: Path, private val key: Long) {

    /**
     * @return a reader positioned after the header, or null if there's no valid snapshot for the current [key].
     */
    fun reader(): SnapshotReader? {
        if (!path.exists()) {
            return null
        }
        return try {
            val reader = SnapshotReader(path.readBytes())
            if (reader.int() == MAGIC && reader.long() == key) reader else null
        } catch (e: Exception) {
            logger.warn(e) { "Unable to read snapshot ${path.name}." }
            null
        }
    }

    /**
     * Decodes the snapshot with [block], which should only apply what it decodes once reading has completed.
     * @return true if the snapshot was valid and [block] completed.
     */
    fun restore(block: (SnapshotReader) -> Unit): Boolean {
        val reader = reader() ?: return false
        return try {
            block(reader)
            true
        } catch (e: Exception) {
            logger.warn(e) { "Unable to restore snapshot ${path.name}." }
            false
        }
    }

    fun write(block: SnapshotWriter.() -> Unit) {
        val temp = path.resolveSibling("${path.name}.${ProcessHandle.current().pid()}.tmp")
        try {
            val writer = SnapshotWriter()
            writer.int(MAGIC)
            writer.long(key)
            writer.block()
            path.createParentDirectories()
            temp.writeBytes(writer.toByteArray())
            Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING)
        } catch (e: Exception) {
            logger.warn(e) { "Unable to write snapshot ${path.name}." }
            temp.deleteIfExists()
            path.deleteIfExists()
        }
    }

    companion object {
        private val logger = InlineLogger()
        private const val MAGIC = 0x564F4944

        /** Increment when the encoding or the content of any snapshot changes. */
        const val VERSION = 1
    }
}

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
    }
}

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
        else -> throw IllegalStateException("Unknown snapshot value tag $tag")
    }
}

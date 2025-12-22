package world.gregs.voidps.cache.type.field.custom

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.config.writePair
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.type.field.AccessibleField
import world.gregs.voidps.cache.type.field.Field
import world.gregs.voidps.cache.type.field.FieldCodec
import world.gregs.voidps.cache.type.field.codec.IntCodec
import world.gregs.voidps.cache.type.field.codec.StringCodec
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator

/**
 * Field for storing dynamic key-value parameters in a Type.
 *
 * Supports sparse storage of additional properties that aren't part of the main Type structure.
 * Values are stored as Map<String, Any> and can be strings or integers in binary format.
 *
 * Keys are mapped to integer IDs for efficient binary storage, and values can be transformed
 * between storage formats (e.g. storing doubles as integers in binary).
 */
class ParameterField(
    size: Int,
    val paramIds: Map<String, Int>,
    val types: Array<FieldCodec<out Any>> = arrayOf(IntCodec, StringCodec),
) : AccessibleField<Map<Int, Any>?> {
    val data = arrayOfNulls<Map<Int, Any>>(size)

    override fun get(index: Int) = data[index]

    override fun set(index: Int, value: Map<Int, Any>?) {
        data[index] = value
    }

    override fun readPacked(reader: Reader, index: Int, opcode: Int) {
        val size = reader.readUnsignedByte()
        if (size == 0) {
            return
        }
        if (data[index] == null) {
            data[index] = Object2ObjectOpenHashMap(size)
        }
        val map = data[index] as MutableMap<Int, Any>
        for (i in 0 until size) {
            val type = reader.readUnsignedByte()
            val id = reader.readUnsignedMedium()
            map[id] = readValue(reader, type)!!
        }
        data[index] = map
    }

    override fun writePacked(writer: Writer, index: Int, opcode: Int): Boolean {
        val params = data[index] ?: return false
        writer.writeByte(opcode)
        writeContent(writer, params)
        return true
    }

    private fun writeContent(writer: Writer, params: Map<Int, Any>) {
        writer.writeByte(params.size)
        for ((id, value) in params) {
            writer.writeByte(type(value))
            writer.writeMedium(id)
            writeValue(writer, value)
        }
    }

    private fun writeValue(writer: Writer, value: Any?) {
        when (value) {
            is Int -> writer.writeInt(value)
            is String -> writer.writeCharString(value)
            is Long -> writer.writeLong(value)
            is Boolean -> writer.writeByte(value)
            is Double -> writer.writeBytes(ByteBuffer.allocate(8).putDouble(value).array())
            is Map<*, *> -> writeMap(writer, value as Map<String, Any>)
            is List<*> -> {
                assert(value.size < 256)
                writer.writeByte(value.size)
                for (value in value) {
                    writer.writeByte(type(value))
                    writeValue(writer, value)
                }
            }
            null -> {}
            else -> throw IllegalArgumentException("Unknown parameter type ${value::class.simpleName} $value")
        }
    }

    private fun type(value: Any?): Int {
        return when (value) {
            is Int -> 0
            is String -> 1
            is Long -> 2
            is Boolean -> 3
            is Double -> 4
            is Map<*, *> -> 5
            is List<*> -> 6
            null -> 7
            else -> throw IllegalArgumentException("Unknown parameter type ${value::class.simpleName} $value")
        }
    }

    private fun readValue(reader: Reader, type: Int): Any? {
        return when (type) {
            0 -> reader.readInt()
            1 -> reader.readCharString()
            2 -> reader.readLong()
            3 -> reader.readBoolean()
            4 -> {
                reader.skip(8)
                ByteBuffer.wrap(reader.array(), reader.position() - 8, 8).getDouble()
            }
            5 -> readMap(reader)
            6 -> Array(reader.readUnsignedByte()) { readValue(reader, reader.readUnsignedByte()) }.toList()
            7 -> null
            else -> throw IllegalArgumentException("Unknown parameter type $type")
        }
    }

    private fun writeMap(writer: Writer, value: Map<String, Any>): Boolean {
        writer.writeByte(value.size)
        for ((id, value) in value) {
            writer.writeByte(type(value))
            writer.writeString(id)
            writeValue(writer, value)
        }
        return true
    }

    private fun readMap(reader: Reader): Map<String, Any> {
        val size = reader.readUnsignedByte()
        val map = Object2ObjectOpenHashMap<String, Any>(size)
        for (i in 0 until size) {
            val type = reader.readUnsignedByte()
            val id = reader.readString()
            map[id] = readValue(reader, type)!!
        }
        return map
    }

    override fun readConfig(reader: ConfigReader, index: Int, key: String) {
        if (data[index] == null) {
            data[index] = HashMap(4)
        }
        val id = paramIds[key] ?: throw IllegalArgumentException("Unknown parameter type $key")
        (data[index] as MutableMap<Int, Any>)[id] = reader.value()
    }

    override fun writeConfig(writer: ConfigWriter, index: Int, key: String): Boolean {
        val id = paramIds[key] ?: throw IllegalArgumentException("Unknown parameter type $key")
        val value = data[index]?.get(id) ?: return false
        writer.writePair(key, value)
        return true
    }

    override fun readDirect(reader: Reader) {
        for (i in 0 until data.size) {
            readPacked(reader, i, 0)
        }
    }

    override fun writeDirect(writer: Writer) {
        for (i in 0 until data.size) {
            val params = data[i]
            if (params == null) {
                writer.writeByte(0)
                continue
            }
            writeContent(writer, params)
        }
    }

    override fun directSize(): Int {
        var size = 0
        for (params in data) {
            if (params == null) {
                size += 1
                continue
            }
            size += 1
            for (value in params.values) {
                size += 4
                size += size(value)
            }
        }
        return size
    }

    private fun size(value: Any?): Int {
        return when (value) {
            is Int -> 4
            is String -> value.length + 1
            is Long, is Double -> 8
            is Boolean -> 1
            is Map<*, *> -> 1 + (value as Map<String, Any>).toList().sumOf { (key, value) -> key.length + 2 + size(value) }
            is List<*> -> 1 + value.sumOf { 1 + size(it) }
            null -> 0
            else -> throw IllegalArgumentException("Unknown parameter type ${value::class.simpleName} $value")
        }
    }

    override fun override(other: Field, from: Int, to: Int) {
        other as ParameterField
        if (other.data[from] == null) {
            return
        }
        data[to] = other.data[from]
    }

    override fun clear() {
        data.fill(null)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ParameterField
        return data.contentEquals(other.data)
    }

    override fun hashCode(): Int {
        return data.contentHashCode()
    }


}
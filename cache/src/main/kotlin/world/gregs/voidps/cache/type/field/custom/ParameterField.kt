package world.gregs.voidps.cache.type.field.custom

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
            data[index] = HashMap(size)
        }
        val map = data[index] as MutableMap<Int, Any>
        for (i in 0 until size) {
            val type = reader.readUnsignedByte()
            val id = reader.readUnsignedMedium()
            map[id] = types[type].readBinary(reader)
        }
        data[index] = map
    }

    override fun writePacked(writer: Writer, index: Int, opcode: Int): Boolean {
        val params = data[index]
        if (params == null) {
            writer.writeByte(0)
            return true
        }
        writer.writeByte(params.size)
        for ((id, value) in params) {
            writeValue(writer, id, value)
        }
        return true
    }

    private fun writeValue(writer: Writer, id: Int, value: Any) {
        when (value) {
            is Int -> {
                writer.writeInt(0 or (id shl 8))
                writer.writeInt(value)
            }
            is String -> {
                writer.writeInt(1 or (id shl 8))
                writer.writeString(value)
            }
            is Long -> {
                writer.writeInt(2 or (id shl 8))
                writer.writeLong(value)
            }
            is Boolean -> {
                writer.writeInt(3 or (id shl 8))
                writer.writeByte(value)
            }
            is Double -> {
                writer.writeInt(4 or (id shl 8))
                writer.writeInt((value * 10.0).toInt())
            }
            is Map<*, *> -> {
                writer.writeInt(5 or (id shl 8))
                value as Map<String, Any>
                writer.writeShort(value.size)
                for ((key, value) in value) {
                    writer.writeString(key)
                    writer.writeString(value.toString())
                }
            }
            is List<*> -> {
                writer.writeInt(6 or (id shl 8))
                writer.writeShort(value.size)
                for (value in value) {
                    writer.writeString(value.toString())
                }
            }
            else -> throw IllegalArgumentException("Unknown parameter type ${value::class.simpleName} $id $value")
        }
    }
    private fun readValue(reader: Reader, type: Int): Any {
        return when (type) {
            0 -> reader.readInt()
            1 -> reader.readString()
            2 -> reader.readLong()
            3 -> reader.readBoolean()
            4 -> reader.readInt() / 10.0
            5 ->{
                val map = HashMap<String, Any>(reader.readShort())
                for (i in 0 until reader.readShort()) {
                    map[reader.readString()] = reader.readString()
                }
                map
            }
            6 -> Array(reader.readShort()) { reader.readString() }.toList()
            else -> throw IllegalArgumentException("Unknown parameter type $type")
        }
    }

    private fun size(value: Any): Int {
        return when (value) {
            is Int, is Double -> 4
            is String -> value.length + 1
            is Long -> 8
            is Boolean -> 1
            is Map<*, *> -> 2 + (value as Map<String, Any>).toList().sumOf { (key, value) -> key.length + 1 + value.toString().length + 1 }
            is List<*> -> 2 + value.sumOf { it.toString().length + 1 }
            else -> throw IllegalArgumentException("Unknown parameter type ${value::class.simpleName} $value")
        }
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
            writePacked(writer, i, 0)
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
}
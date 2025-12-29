package world.gregs.voidps.cache.definition.types

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer
import kotlin.text.ifEmpty

interface Types {
    fun load(reader: Reader)
    fun save(writer: Writer)

    fun finish() {}
}

object Helpers {
    @JvmName("readStringsNullable")
    fun readStrings(reader: Reader, array: Array<String?>) {
        for (i in array.indices) {
            val string = reader.readString()
            array[i] = string.ifEmpty { null }
        }
    }

    fun readStrings(reader: Reader, array: Array<String>) {
        for (i in array.indices) {
            array[i] = reader.readString()
        }
    }

    @JvmName("writeStringsNullable")
    fun writeStrings(writer: Writer, array: Array<String?>) {
        for (string in array) {
            writer.writeString(string)
        }
    }

    fun writeStrings(writer: Writer, array: Array<String>) {
        for (string in array) {
            writer.writeString(string)
        }
    }

    fun readIntArrays(reader: Reader, arrays: Array<IntArray?>) {
        for (i in arrays.indices) {
            val size = reader.readUnsignedByte()
            if (size == 0) {
                continue
            }
            val array = IntArray(size)
            reader.readBytes(array)
            arrays[i] = array
        }
    }

    fun writeIntArrays(writer: Writer, arrays: Array<IntArray?>) {
        for (array in arrays) {
            if (array == null) {
                writer.writeByte(0)
            } else {
                writer.writeByte(array.size)
                writer.writeBytes(array)
            }
        }
    }

    fun readExtras(reader: Reader, extras: Array<Map<String, Any>?>) {
        for (i in extras.indices) {
            val size = reader.readUnsignedByte()
            if (size == 0) {
                extras[i] = null
                continue
            }
            val map = mutableMapOf<String, Any>()
            for (i in 0 until size) {
                val type = reader.readUnsignedByte()
                val id = reader.readString()
                if (type == 0) {
                    map[id] = reader.readString()
                } else if (type == 1) {
                    map[id] = reader.readInt()
                }
            }
        }
    }

    fun writeExtras(writer: Writer, extras: Array<Map<String, Any>?>) {
        for (params in extras) {
            if (params == null) {
                writer.writeByte(0)
                continue
            }
            writer.writeByte(params.size)
            for ((id, value) in params) {
                val type = if (value is String) 1 else 0
                writer.writeByte(type)
                writer.writeString(id)
                if (value is String) {
                    writer.writeString(value)
                } else if (value is Int) {
                    writer.writeInt(value)
                }
            }
        }
    }
}
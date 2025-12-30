package world.gregs.voidps.cache.definition.types

import world.gregs.config.Config
import world.gregs.config.ConfigReader
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer
import java.nio.ByteBuffer
import kotlin.text.ifEmpty

abstract class Types {
    internal open fun bytes(): List<ByteArray> = emptyList()
    internal open fun shorts(): List<ShortArray> = emptyList()
    internal open fun ints(): List<IntArray> = emptyList()
    internal open fun doubles(): List<DoubleArray> = emptyList()
    internal open fun strings(): List<Array<String>> = emptyList()
    internal open fun nullAnys(): List<Array<Any?>> = emptyList()
    internal open fun nullStrings(): List<Array<String?>> = emptyList()
    internal open fun nullIntArrays(): List<Array<IntArray?>> = emptyList()
    internal open fun nullStringSets(): List<Array<Set<String>?>> = emptyList()
    internal open fun nullStringArrays(): List<Array<Array<String>?>> = emptyList()
    internal open fun nullMaps(): List<Array<Map<String, Any>?>> = emptyList()

    abstract fun set(size: Int)

    fun load(reader: Reader) {
        for (array in bytes()) {
            reader.readBytes(array)
        }
        for (array in shorts()) {
            reader.readBytes(array)
        }
        for (array in ints()) {
            reader.readBytes(array)
        }
        for (array in doubles()) {
            reader.readBytes(array)
        }
        for (array in strings()) {
            for (i in array.indices) {
                array[i] = reader.readString()
            }
        }
        for (array in nullStrings()) {
            for (i in array.indices) {
                val string = reader.readString()
                array[i] = string.ifEmpty { null }
            }
        }
        for (array in nullAnys()) {
            for (i in array.indices) {
                val type = reader.readUnsignedByte()
                array[i] = readType(reader, type)
            }
        }
        for (sets in nullStringSets()) {
            for (i in sets.indices) {
                val size = reader.readUnsignedByte()
                if (size == 0) {
                    sets[i] = null
                    continue
                }
                val set = mutableSetOf<String>()
                for (i in 0 until size) {
                    set.add(reader.readString())
                }
                sets[i] = set
            }
        }
        for (arrays in nullStringArrays()) {
            for (i in arrays.indices) {
                val size = reader.readUnsignedByte()
                if (size == 0) {
                    arrays[i] = null
                    continue
                }
                arrays[i] = Array(size) { reader.readString() }
            }
        }
        for (arrays in nullIntArrays()) {
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
        for (maps in nullMaps()) {
            for (i in maps.indices) {
                val size = reader.readUnsignedByte()
                if (size == 0) {
                    maps[i] = null
                    continue
                }
                val map = mutableMapOf<String, Any>()
                for (i in 0 until size) {
                    val type = reader.readUnsignedByte()
                    val id = reader.readString()
                    map[id] = readType(reader, type) ?: throw IllegalArgumentException("Unexpected null map value $id")
                }
            }
        }
    }

    private fun type(value: Any?) = when (value) {
        is Int -> 0
        is String -> 1
        is Long -> 2
        is Double -> 3
        is Boolean -> 4
        null -> 5
        else -> throw IllegalArgumentException("Unexpected value type ${value::class.simpleName} $value")
    }

    private fun writeType(writer: Writer, value: Any?) = when (value) {
        is Int -> writer.writeInt(value)
        is String -> writer.writeString(value)
        is Long -> writer.writeLong(value)
        is Double -> writer.writeBytes(ByteBuffer.allocate(8).putDouble(value).array())
        is Boolean -> writer.writeByte(value)
        null -> {}
        else -> throw IllegalArgumentException("Invalid type ${value.javaClass.simpleName} $value")
    }

    private fun readType(reader: Reader, type: Int): Any? = when (type) {
        0 -> reader.readInt()
        1 -> reader.readString()
        2 -> reader.readLong()
        3 -> {
            val buffer = ByteBuffer.allocate(8)
            reader.readBytes(buffer.array())
            buffer.getDouble()
        }
        4 -> reader.readBoolean()
        5 -> null
        else -> throw IllegalArgumentException("Unexpected type $type")
    }

    fun save(writer: Writer) {
        for (array in bytes()) {
            writer.writeBytes(array)
        }
        for (array in shorts()) {
            writer.writeBytes(array)
        }
        for (array in ints()) {
            writer.writeBytes(array)
        }
        for (array in doubles()) {
            writer.writeBytes(array)
        }
        for (array in strings()) {
            for (string in array) {
                writer.writeString(string)
            }
        }
        for (array in nullStrings()) {
            for (string in array) {
                writer.writeString(string)
            }
        }
        for (array in nullAnys()) {
            for (value in array) {
                writer.writeByte(type(value))
                writeType(writer, value)
            }
        }
        for (sets in nullStringSets()) {
            for (set in sets) {
                if (set == null) {
                    writer.writeByte(0)
                    continue
                }
                writer.writeByte(set.size)
                for (string in set) {
                    writer.writeString(string)
                }
            }
        }
        for (arrays in nullStringArrays()) {
            for (array in arrays) {
                if (array == null) {
                    writer.writeByte(0)
                    continue
                }
                writer.writeByte(array.size)
                for (string in array) {
                    writer.writeString(string)
                }
            }
        }
        for (arrays in nullIntArrays()) {
            for (array in arrays) {
                if (array == null) {
                    writer.writeByte(0)
                    continue
                }
                writer.writeByte(array.size)
                writer.writeBytes(array)
            }
        }
        for (maps in nullMaps()) {
            for (params in maps) {
                if (params == null) {
                    writer.writeByte(0)
                    continue
                }
                writer.writeByte(params.size)
                for ((id, value) in params) {
                    val type = type(value)
                    writer.writeByte(type)
                    writer.writeString(id)
                    writeType(writer, value)
                }
            }
        }
    }

    open fun load(key: String, value: Any, id: Int, section: String): Boolean {
        return false
    }

    open fun before(section: String) {

    }

    open fun after(id: Int, section: String) {

    }

    fun load(paths: List<String>, stringLength: Int = 100) {
        for (path in paths) {
            Config.fileReader(path, stringLength) {
                while (nextSection()) {
                    val section = section()
                    before(section)
                    var id = -1
                    while (nextPair()) {
                        val key = key()
                        if (key == "id") {
                            id = int()
                            continue
                        }
                        val value = value()
                        val loaded = load(key, value, id, section)
                        if (loaded) {
                            continue
                        }
                        unloaded(this, key, value, id, section)
                    }
                    after(id, section)
                }
            }
        }
    }

    open fun unloaded(reader: ConfigReader, key: String, value: Any, id: Int, section: String) {
        throw IllegalArgumentException("Unexpected key: '$key' in $section ${reader.exception()}")
    }

}

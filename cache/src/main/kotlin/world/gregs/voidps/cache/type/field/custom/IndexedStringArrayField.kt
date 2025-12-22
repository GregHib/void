package world.gregs.voidps.cache.type.field.custom

import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.config.writePair
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.type.field.FieldCodec
import world.gregs.voidps.cache.type.field.AccessibleField
import world.gregs.voidps.cache.type.field.Field
import world.gregs.voidps.cache.type.field.codec.NullStringCodec
import world.gregs.voidps.cache.type.field.codec.StringCodec

/**
 * Used for options
 *
 * ```
 * options[opcode - 35] = buffer.readString()
 * ```
 */
class IndexedStringArrayField(
    size: Int,
    val default: Array<String?>,
    val offset: Int,
    val field: FieldCodec<String?> = NullStringCodec,
) : AccessibleField<Array<String?>> {
    val data = Array(size) { default.clone() }

    override fun get(index: Int): Array<String?> = data[index]

    override fun set(index: Int, value: Array<String?>) {
        data[index] = value
    }

    override fun readPacked(reader: Reader, index: Int, opcode: Int) {
        get(index)[opcode - offset] = field.readBinary(reader)
    }

    override fun writePacked(writer: Writer, index: Int, opcode: Int): Boolean {
        val value = get(index)[opcode - offset]
        if (value == default[opcode - offset]) {
            return false
        }
        writer.writeByte(opcode)
        field.writeBinary(writer, value)
        return true
    }

    override fun readConfig(reader: ConfigReader, index: Int, key: String) {
        val array = get(index)
        var count = 0
        while (reader.nextElement()) {
            val value = reader.string()
            array[count++] = if (value == "null") null else value
        }
    }

    override fun writeConfig(writer: ConfigWriter, index: Int, key: String): Boolean {
        val value = get(index)
        if (value.contentEquals(default)) {
            return false
        }
        writer.writePair(key, value)
        return true
    }

    override fun readDirect(reader: Reader) {
        // String-key table for faster decoding
        val readKeys = Array(reader.readUnsignedShort()) { if (it == 0) null else reader.readString() }
        for (i in 0 until data.size) {
            val options = data[i]
            while (true) {
                val index = reader.readUnsignedByte()
                if (index > options.size) {
                    break
                }
                val id = reader.readUnsignedShort()
                options[index] = readKeys[id]
            }
        }
    }

    override fun writeDirect(writer: Writer) {
        val keys = data.map { it.toList() }.flatten().distinct().sortedBy { it }
        writer.writeShort(keys.size)
        for (key in keys) {
            if (key == null) continue
            writer.writeString(key)
        }
        val map = keys.mapIndexed { it, s -> s to it }.toMap()
        for (options in data) {
            for (i in options.indices) {
                val option = options[i]
                if (option == default[i]) {
                    continue
                }
                writer.writeByte(i)
                writer.writeShort(map.getValue(option))
            }
            writer.writeByte(options.size + 1)
        }
    }

    override fun override(other: Field, from: Int, to: Int) {
        other as IndexedStringArrayField
        if (other.data[from].contentEquals(default)) {
            return
        }
        data[to] = other.data[from]
    }

    override fun directSize(): Int {
        val keys = data.map { it.toList() }.flatten().distinct().sortedBy { it }.filterNotNull()
        return 2 + keys.sumOf { StringCodec.bytes(it) } + data.size + data.sumOf { it.indices.count { i -> it[i] != null && it[i] != default[i] } * 3 }
    }

    override fun clear() {
        for (i in data.indices) {
            data[i] = default.clone()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IndexedStringArrayField

        return data.contentDeepEquals(other.data)
    }

    override fun hashCode() = data.contentDeepHashCode()

}

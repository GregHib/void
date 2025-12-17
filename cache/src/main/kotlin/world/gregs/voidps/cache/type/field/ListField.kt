package world.gregs.voidps.cache.type.field

import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.config.writeValue
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer

/**
 * Field for non-nullable lists with non-nullable elements: List<T>
 */
class ListField<T : Any>(
    key: String,
    default: List<T>,
    val field: ValueField<T>,
) : ValueField<List<T>>(key, default) {
    override fun readBinary(reader: Reader): List<T> {
        val length = reader.readByte()
        val list = mutableListOf<T>()
        for (i in 0 until length) {
            list.add(field.readBinary(reader))
        }
        return list
    }

    override fun writeBinary(writer: Writer, value: List<T>) {
        writer.writeByte(value.size)
        for (v in value) {
            field.writeBinary(writer, v)
        }
    }

    override fun readConfig(reader: ConfigReader): List<T> {
        val list = mutableListOf<T>()
        while (reader.nextElement()) {
            list.add(field.readConfig(reader))
        }
        return list
    }

    override fun writeConfig(writer: ConfigWriter, value: List<T>) {
        writer.writeValue(value)
    }
}

/**
 * Field for nullable lists with non-nullable elements: List<T>?
 * Empty lists and null are both serialized as length 0 in binary.
 */
class NullListField<T : Any>(
    key: String,
    default: List<T>?,
    val field: ValueField<T>,
) : ValueField<List<T>?>(key, default) {
    override fun readBinary(reader: Reader): List<T> {
        val length = reader.readByte()
        val list = mutableListOf<T>()
        for (i in 0 until length) {
            list.add(field.readBinary(reader))
        }
        return list
    }

    override fun writeBinary(writer: Writer, value: List<T>?) {
        if (value == null || value.isEmpty()) {
            writer.writeByte(0)
            return
        }
        writer.writeByte(value.size)
        for (v in value) {
            field.writeBinary(writer, v)
        }
    }

    override fun readConfig(reader: ConfigReader): List<T> {
        val list = mutableListOf<T>()
        while (reader.nextElement()) {
            list.add(field.readConfig(reader))
        }
        return list
    }

    override fun writeConfig(writer: ConfigWriter, value: List<T>?) {
        writer.writeValue(value)
    }
}

/**
 * Field for non-nullable lists with nullable elements: List<T?>
 */
class ListNullField<T : Any>(
    key: String,
    default: List<T?>,
    val field: ValueField<T?>,
) : ValueField<List<T?>>(key, default) {
    override fun readBinary(reader: Reader): List<T?> {
        val length = reader.readByte()
        val list = mutableListOf<T?>()
        for (i in 0 until length) {
            list.add(field.readBinary(reader))
        }
        return list
    }

    override fun writeBinary(writer: Writer, value: List<T?>) {
        if (value.isEmpty()) {
            writer.writeByte(0)
            return
        }
        writer.writeByte(value.size)
        for (v in value) {
            field.writeBinary(writer, v)
        }
    }

    override fun readConfig(reader: ConfigReader): List<T?> {
        val list = mutableListOf<T?>()
        while (reader.nextElement()) {
            list.add(field.readConfig(reader))
        }
        return list
    }

    override fun writeConfig(writer: ConfigWriter, value: List<T?>) {
        writer.writeValue(value)
    }
}

/**
 * Field for nullable lists with nullable elements: List<T?>?
 */
class NullListNullField<T : Any>(
    key: String,
    default: List<T?>?,
    val field: ValueField<T?>,
) : ValueField<List<T?>?>(key, default) {
    override fun readBinary(reader: Reader): List<T?> {
        val length = reader.readByte()
        val list = mutableListOf<T?>()
        for (i in 0 until length) {
            list.add(field.readBinary(reader))
        }
        return list
    }

    override fun writeBinary(writer: Writer, value: List<T?>?) {
        if (value == null || value.isEmpty()) {
            writer.writeByte(0)
            return
        }
        writer.writeByte(value.size)
        for (v in value) {
            field.writeBinary(writer, v)
        }
    }

    override fun readConfig(reader: ConfigReader): List<T?> {
        val list = mutableListOf<T?>()
        while (reader.nextElement()) {
            list.add(field.readConfig(reader))
        }
        return list
    }

    override fun writeConfig(writer: ConfigWriter, value: List<T?>?) {
        writer.writeValue(value)
    }
}
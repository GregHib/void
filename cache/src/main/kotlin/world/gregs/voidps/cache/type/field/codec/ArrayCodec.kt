package world.gregs.voidps.cache.type.field.codec

import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.config.list
import world.gregs.config.writeValue
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.type.field.FieldCodec

object StringArrayCodec : ArrayCodec<String>(StringCodec, create = { size, block -> Array(size, block) })
object NullStringArrayCodec : NullArrayCodec<String>(StringCodec, create = { size, block -> Array(size, block) })

open class ArrayCodec<T>(
    val field: FieldCodec<T>,
    val size: FieldCodec<Int> = UnsignedByteCodec,
    val create: (Int, block: (Int) -> T) -> Array<T>,
) : FieldCodec<Array<T>> {
    override fun bytes(value: Array<T>): Int = size.bytes(value.size) + value.sumOf { field.bytes(it) }
    override fun readBinary(reader: Reader) = create(size.readBinary(reader)) { field.readBinary(reader) }

    override fun writeBinary(writer: Writer, value: Array<T>) {
        size.writeBinary(writer, value.size)
        for (v in value) {
            field.writeBinary(writer, v)
        }
    }

    override fun readConfig(reader: ConfigReader): Array<T> {
        val list = mutableListOf<T>()
        while (reader.nextElement()) {
            list.add(field.readConfig(reader))
        }
        return create(list.size) { list[it] }
    }

    override fun writeConfig(writer: ConfigWriter, value: Array<T>) {
        writer.list(value.size) {
            writer.writeValue(value[it])
        }
    }
}

open class NullArrayCodec<T>(
    val field: FieldCodec<T>,
    val size: FieldCodec<Int> = UnsignedByteCodec,
    val create: (Int, block: (Int) -> T) -> Array<T>,
) : FieldCodec<Array<T>?> {
    override fun bytes(value: Array<T>?): Int = if (value == null) size.bytes(0) else size.bytes(value.size) + value.sumOf { field.bytes(it) }
    override fun readBinary(reader: Reader): Array<T>? {
        val size = size.readBinary(reader)
        if (size == 0) {
            return null
        }
        return create(size) { field.readBinary(reader) }
    }

    override fun writeBinary(writer: Writer, value: Array<T>?) {
        if (value == null) {
            size.writeBinary(writer, 0)
            return
        }
        size.writeBinary(writer, value.size)
        for (v in value) {
            field.writeBinary(writer, v)
        }
    }

    override fun readConfig(reader: ConfigReader): Array<T>? {
        if (reader.peek == '"') {
            val value = reader.string()
            require(value == "null") { "Expected null Array, found: $value" }
            return null
        }
        val list = mutableListOf<T>()
        while (reader.nextElement()) {
            list.add(field.readConfig(reader))
        }
        return create(list.size) { list[it] }
    }

    override fun writeConfig(writer: ConfigWriter, value: Array<T>?) {
        if (value == null) {
            writer.writeValue("null")
            return
        }
        writer.list(value.size) {
            writer.writeValue(value[it])
        }
    }
}
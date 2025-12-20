package world.gregs.voidps.cache.type.field.codec

import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.config.list
import world.gregs.config.writeValue
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.type.field.FieldCodec

open class IntArrayCodec(val field: FieldCodec<Int>, val size: FieldCodec<Int> = UnsignedByteCodec) : FieldCodec<IntArray> {
    override fun bytes(value: IntArray): Int = size.bytes(0) + value.size * field.bytes(0)
    override fun readBinary(reader: Reader) = IntArray(size.readBinary(reader)) { field.readBinary(reader) }

    override fun writeBinary(writer: Writer, value: IntArray) {
        size.writeBinary(writer, value.size)
        for (v in value) {
            field.writeBinary(writer, v)
        }
    }

    override fun readConfig(reader: ConfigReader): IntArray {
        val list = mutableListOf<Int>()
        while (reader.nextElement()) {
            list.add(field.readConfig(reader))
        }
        return list.toIntArray()
    }

    override fun writeConfig(writer: ConfigWriter, value: IntArray) {
        writer.list(value.size) {
            writer.writeValue(value[it])
        }
    }

    companion object : IntArrayCodec(IntCodec)
}

open class NullIntArrayCodec(val field: FieldCodec<Int>, val size: FieldCodec<Int> = UnsignedByteCodec) : FieldCodec<IntArray?> {
    override fun bytes(value: IntArray?): Int = size.bytes(-1) + if (value != null) value.size * field.bytes(-1) else 0
    override fun readBinary(reader: Reader): IntArray? {
        val size = size.readBinary(reader)
        if (size == 0) {
            return null
        }
        return IntArray(size) { field.readBinary(reader) }
    }

    override fun writeBinary(writer: Writer, value: IntArray?) {
        if (value == null) {
            size.writeBinary(writer, 0)
            return
        }
        size.writeBinary(writer, value.size)
        for (v in value) {
            field.writeBinary(writer, v)
        }
    }

    override fun readConfig(reader: ConfigReader): IntArray? {
        if (reader.peek == '"') {
            val value = reader.string()
            require(value == "null") { "Expected null IntArray, found: $value" }
            return null
        }
        val list = mutableListOf<Int>()
        while (reader.nextElement()) {
            list.add(field.readConfig(reader))
        }
        return list.toIntArray()
    }

    override fun writeConfig(writer: ConfigWriter, value: IntArray?) {
        if (value == null) {
            writer.writeValue("null")
            return
        }
        writer.list(value.size) {
            writer.writeValue(value[it])
        }
    }

    companion object : NullIntArrayCodec(IntCodec)
}
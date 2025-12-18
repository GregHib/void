package world.gregs.voidps.cache.type.field.codec

import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.config.list
import world.gregs.config.writeValue
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.type.field.FieldCodec

open class IntArrayCodec(val field: FieldCodec<Int>, val size: FieldCodec<Int> = UnsignedByteCodec) : FieldCodec<IntArray> {
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
    override fun readBinary(reader: Reader) = IntArray(size.readBinary(reader)) { field.readBinary(reader) }

    override fun writeBinary(writer: Writer, value: IntArray?) {
        if (value == null) {
            return
        }
        size.writeBinary(writer, value.size)
        for (v in value) {
            field.writeBinary(writer, v)
        }
    }

    override fun readConfig(reader: ConfigReader): IntArray? {
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
package world.gregs.voidps.cache.type.field.codec

import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.config.list
import world.gregs.config.writeValue
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.type.field.FieldCodec

open class ShortArrayCodec(val field: FieldCodec<Int>, val size: FieldCodec<Int> = UnsignedByteCodec) : FieldCodec<ShortArray> {
    override fun readBinary(reader: Reader) = ShortArray(size.readBinary(reader)) { field.readBinary(reader).toShort() }

    override fun writeBinary(writer: Writer, value: ShortArray) {
        size.writeBinary(writer, value.size)
        for (v in value) {
            field.writeBinary(writer, v.toInt())
        }
    }

    override fun readConfig(reader: ConfigReader): ShortArray {
        val list = mutableListOf<Short>()
        while (reader.nextElement()) {
            list.add(field.readConfig(reader).toShort())
        }
        return list.toShortArray()
    }

    override fun writeConfig(writer: ConfigWriter, value: ShortArray) {
        writer.list(value.size) {
            writer.writeValue(value[it])
        }
    }

    companion object : ShortArrayCodec(ShortCodec)
}

open class NullShortArrayCodec(val field: FieldCodec<Int>, val size: FieldCodec<Int> = UnsignedByteCodec) : FieldCodec<ShortArray?> {
    override fun readBinary(reader: Reader) = ShortArray(size.readBinary(reader)) { field.readBinary(reader).toShort() }

    override fun writeBinary(writer: Writer, value: ShortArray?) {
        if (value == null) {
            return
        }
        size.writeBinary(writer, value.size)
        for (v in value) {
            field.writeBinary(writer, v.toInt())
        }
    }

    override fun readConfig(reader: ConfigReader): ShortArray? {
        val list = mutableListOf<Short>()
        while (reader.nextElement()) {
            list.add(field.readConfig(reader).toShort())
        }
        return list.toShortArray()
    }

    override fun writeConfig(writer: ConfigWriter, value: ShortArray?) {
        if (value == null) {
            writer.writeValue("null")
            return
        }
        writer.list(value.size) {
            writer.writeValue(value[it])
        }
    }

    companion object : NullShortArrayCodec(ShortCodec)
}
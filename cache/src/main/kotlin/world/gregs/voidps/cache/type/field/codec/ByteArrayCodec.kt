package world.gregs.voidps.cache.type.field.codec

import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.config.list
import world.gregs.config.writeValue
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.type.field.FieldCodec

open class ByteArrayCodec(val field: FieldCodec<Int>, val size: FieldCodec<Int> = UnsignedByteCodec) : FieldCodec<ByteArray> {
    override fun readBinary(reader: Reader) = ByteArray(size.readBinary(reader)) { field.readBinary(reader).toByte() }

    override fun writeBinary(writer: Writer, value: ByteArray) {
        size.writeBinary(writer, value.size)
        for (v in value) {
            field.writeBinary(writer, v.toInt())
        }
    }

    override fun readConfig(reader: ConfigReader): ByteArray {
        val list = mutableListOf<Byte>()
        while (reader.nextElement()) {
            list.add(field.readConfig(reader).toByte())
        }
        return list.toByteArray()
    }

    override fun writeConfig(writer: ConfigWriter, value: ByteArray) {
        writer.list(value.size) {
            writer.writeValue(value[it])
        }
    }

    companion object : ByteArrayCodec(ByteCodec)
}

open class NullByteArrayCodec(val field: FieldCodec<Int>, val size: FieldCodec<Int> = UnsignedByteCodec) : FieldCodec<ByteArray?> {
    override fun readBinary(reader: Reader): ByteArray? {
        val size = size.readBinary(reader)
        if (size == -1) {
            return null
        }
        return ByteArray(size) { field.readBinary(reader).toByte() }
    }

    override fun writeBinary(writer: Writer, value: ByteArray?) {
        if (value == null) {
            size.writeBinary(writer, -1)
            return
        }
        size.writeBinary(writer, value.size)
        for (v in value) {
            field.writeBinary(writer, v.toInt())
        }
    }

    override fun readConfig(reader: ConfigReader): ByteArray? {
        if (reader.peek == '"') {
            val value = reader.string()
            require(value == "null") { "Expected null ShortArray, found: $value" }
            return null
        }
        val list = mutableListOf<Byte>()
        while (reader.nextElement()) {
            list.add(field.readConfig(reader).toByte())
        }
        return list.toByteArray()
    }

    override fun writeConfig(writer: ConfigWriter, value: ByteArray?) {
        if (value == null) {
            writer.writeValue("null")
            return
        }
        writer.list(value.size) {
            writer.writeValue(value[it])
        }
    }

    companion object : NullByteArrayCodec(ByteCodec)
}
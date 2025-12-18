package world.gregs.voidps.cache.type.field.codec

import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.config.list
import world.gregs.config.map
import world.gregs.config.writeValue
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.type.field.FieldCodec
import world.gregs.voidps.cache.type.field.ValueField

class MapCodec(
    val fields: Map<String, FieldCodec<out Any>>,
    val size: FieldCodec<Int> = UnsignedByteCodec
) : FieldCodec<Map<String, Any>> {

    constructor(vararg fields: Pair<String, FieldCodec<out Any>>) : this(fields.toMap())

    override fun readBinary(reader: Reader): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        val size = size.readBinary(reader)
        for (i in 0 until size) {
            val key = reader.readString()
            map[key] = fields[key]!!.readBinary(reader)
        }
        return map
    }

    @Suppress("UNCHECKED_CAST")
    override fun writeBinary(writer: Writer, value: Map<String, Any>) {
        size.writeBinary(writer, value.size)
        for ((key, v) in value) {
            (fields[key]!! as FieldCodec<Any>).writeBinary(writer, v)
        }
    }

    override fun readConfig(reader: ConfigReader): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        while (reader.nextEntry()) {
            map[reader.key()] = reader.value()
        }
        return map
    }

    override fun writeConfig(writer: ConfigWriter, value: Map<String, Any>) {
        writer.map(value.keys) { key ->
            writer.writeValue(value[key])
        }
    }
}

class NullMapCodec(
    val fields: Map<String, FieldCodec<out Any>>,
    val size: FieldCodec<Int> = UnsignedByteCodec
) : FieldCodec<Map<String, Any>?> {
    override fun readBinary(reader: Reader): Map<String, Any>? {
        val size = size.readBinary(reader)
        if (size == -1) {
            return null
        }
        val map = mutableMapOf<String, Any>()
        for (i in 0 until size) {
            val key = reader.readString()
            map[key] = fields[key]!!.readBinary(reader)
        }
        return map
    }

    @Suppress("UNCHECKED_CAST")
    override fun writeBinary(writer: Writer, value: Map<String, Any>?) {
        if (value == null) {
            size.writeBinary(writer, -1)
            return
        }
        size.writeBinary(writer, value.size)
        for ((key, v) in value) {
            (fields[key]!! as FieldCodec<Any>).writeBinary(writer, v)
        }
    }

    override fun readConfig(reader: ConfigReader): Map<String, Any>? {
        if (reader.peek == '"') {
            val value = reader.string()
            require(value == "null") { "Expected null map, found: $value" }
            return null
        }
        val map = mutableMapOf<String, Any>()
        while (reader.nextEntry()) {
            map[reader.key()] = reader.value()
        }
        return map
    }

    override fun writeConfig(writer: ConfigWriter, value: Map<String, Any>?) {
        if (value == null) {
            writer.writeValue("null")
            return
        }
        writer.map(value.keys) { key ->
            writer.writeValue(value[key])
        }
    }

}
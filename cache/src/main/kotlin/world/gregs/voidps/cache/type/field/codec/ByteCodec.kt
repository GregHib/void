package world.gregs.voidps.cache.type.field.codec

import world.gregs.voidps.cache.type.field.FieldCodec
import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.config.writeValue
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer

/**
 * Field for non-nullable byte values.
 */
object ByteCodec : FieldCodec<Int> {
    override fun readBinary(reader: Reader) = reader.readByte()
    override fun writeBinary(writer: Writer, value: Int) = writer.writeByte(value)
    override fun readConfig(reader: ConfigReader) = reader.int()
    override fun writeConfig(writer: ConfigWriter, value: Int) = writer.writeValue(value)
}

object UnsignedByteCodec : FieldCodec<Int> {
    override fun readBinary(reader: Reader) = reader.readUnsignedByte()
    override fun writeBinary(writer: Writer, value: Int) = writer.writeByte(value)
    override fun readConfig(reader: ConfigReader) = reader.int()
    override fun writeConfig(writer: ConfigWriter, value: Int) = writer.writeValue(value)
}
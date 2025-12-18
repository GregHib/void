package world.gregs.voidps.cache.type.field.codec

import world.gregs.voidps.cache.type.field.FieldCodec
import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.config.writeValue
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer

object ShortCodec : FieldCodec<Int> {
    override fun readBinary(reader: Reader) = reader.readShort()
    override fun writeBinary(writer: Writer, value: Int) = writer.writeShort(value)
    override fun readConfig(reader: ConfigReader) = reader.int()
    override fun writeConfig(writer: ConfigWriter, value: Int) = writer.writeValue(value)
}

object UnsignedShortCodec : FieldCodec<Int> {
    override fun readBinary(reader: Reader) = reader.readUnsignedShort()
    override fun writeBinary(writer: Writer, value: Int) = writer.writeShort(value)
    override fun readConfig(reader: ConfigReader) = reader.int()
    override fun writeConfig(writer: ConfigWriter, value: Int) = writer.writeValue(value)
}
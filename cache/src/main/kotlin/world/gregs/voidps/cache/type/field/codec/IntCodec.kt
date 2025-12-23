package world.gregs.voidps.cache.type.field.codec

import world.gregs.voidps.cache.type.field.FieldCodec
import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.config.writeValue
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer

object IntCodec : FieldCodec<Int> {
    override fun bytes(value: Int) = 4
    override fun readBinary(reader: Reader) = reader.readInt()
    override fun writeBinary(writer: Writer, value: Int) = writer.writeInt(value)
    override fun readConfig(reader: ConfigReader) = reader.int()
    override fun writeConfig(writer: ConfigWriter, value: Int) = writer.writeValue(value)
}

object SmartCodec : FieldCodec<Int> {
    override fun bytes(value: Int) = if (value >= 128) 2 else 1
    override fun readBinary(reader: Reader) = reader.readSmart()
    override fun writeBinary(writer: Writer, value: Int) = writer.writeSmart(value)
    override fun readConfig(reader: ConfigReader) = reader.int()
    override fun writeConfig(writer: ConfigWriter, value: Int) = writer.writeValue(value)
}

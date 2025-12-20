package world.gregs.voidps.cache.type.field.codec

import world.gregs.voidps.cache.type.field.FieldCodec
import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.config.writeValue
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer

object LongCodec : FieldCodec<Long> {
    override fun bytes(value: Long) = 8
    override fun readBinary(reader: Reader) = reader.readLong()
    override fun writeBinary(writer: Writer, value: Long) = writer.writeLong(value)
    override fun readConfig(reader: ConfigReader) = reader.long()
    override fun writeConfig(writer: ConfigWriter, value: Long) = writer.writeValue(value)
}

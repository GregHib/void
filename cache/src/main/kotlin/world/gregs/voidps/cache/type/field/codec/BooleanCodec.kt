package world.gregs.voidps.cache.type.field.codec

import world.gregs.voidps.cache.type.field.FieldCodec
import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.config.writeValue
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer

/**
 * Codec for non-nullable Boolean values.
 */
object BooleanCodec : FieldCodec<Boolean> {
    override fun bytes(value: Boolean) = 1
    override fun readBinary(reader: Reader) = reader.readBoolean()
    override fun writeBinary(writer: Writer, value: Boolean) = writer.writeByte(value)
    override fun readConfig(reader: ConfigReader) = reader.boolean()
    override fun writeConfig(writer: ConfigWriter, value: Boolean) = writer.writeValue(value)
}
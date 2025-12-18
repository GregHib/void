package world.gregs.voidps.cache.type.field.codec

import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.type.field.FieldCodec

/**
 * Codec which doesn't read or write any binary values but returns a [fixed] value.
 */
class LiteralCodec<T: Any>(val fixed: T, val codec: FieldCodec<T>) : FieldCodec<T> {
    override fun readBinary(reader: Reader) = fixed
    override fun writeBinary(writer: Writer, value: T) {}
    override fun readConfig(reader: ConfigReader): T = codec.readConfig(reader)
    override fun writeConfig(writer: ConfigWriter, value: T) = codec.writeConfig(writer, value)
}
package world.gregs.voidps.cache.type.field.codec

import world.gregs.voidps.cache.type.field.FieldCodec
import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.config.writeValue
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer

object DoubleCodec : FieldCodec<Double> {
    override fun bytes(value: Double) = 4
    override fun readBinary(reader: Reader) = reader.readInt() / 10.0
    override fun writeBinary(writer: Writer, value: Double) = writer.writeInt((value * 10).toInt())
    override fun readConfig(reader: ConfigReader) = reader.double()
    override fun writeConfig(writer: ConfigWriter, value: Double) = writer.writeValue(value)
}

package world.gregs.config.param.codec

import world.gregs.config.ConfigReader
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer
import java.nio.ByteBuffer

object DoubleParam : ParamCodec<Double>() {
    override fun read(reader: Reader): Double {
        val value = ByteBuffer.wrap(reader.array()).getDouble(reader.position())
        reader.position(reader.position() + 8)
        return value
    }
    override fun read(reader: ConfigReader) = reader.double()
    override fun write(writer: Writer, value: Double) {
        ByteBuffer.wrap(writer.array()).putDouble(writer.position(), value)
        writer.position(writer.position() + 8)
    }
}
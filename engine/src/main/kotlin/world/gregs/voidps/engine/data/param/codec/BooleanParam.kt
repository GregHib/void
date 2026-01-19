package world.gregs.voidps.engine.data.param.codec

import world.gregs.config.ConfigReader
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer

object BooleanParam : ParamCodec<Boolean>() {
    override fun read(reader: Reader) = reader.readBoolean()
    override fun read(reader: ConfigReader) = reader.boolean()
    override fun write(writer: Writer, value: Boolean) {
        writer.writeByte(value)
    }
}
package world.gregs.config.param.codec

import world.gregs.config.ConfigReader
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer

object StringParam : ParamCodec<String>() {
    override fun read(reader: Reader) = reader.readString()
    override fun read(reader: ConfigReader) = reader.string()
    override fun write(writer: Writer, value: String) {
        writer.writeString(value)
    }
}
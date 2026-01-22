package world.gregs.config.param.codec

import world.gregs.config.ConfigReader
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer

object IntParam : ParamCodec<Int>() {
    override fun read(reader: Reader) = reader.readInt()
    override fun read(reader: ConfigReader) = reader.int()
    override fun write(writer: Writer, value: Int) {
        writer.writeInt(value)
    }
}
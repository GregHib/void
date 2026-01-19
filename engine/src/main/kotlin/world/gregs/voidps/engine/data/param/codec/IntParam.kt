package world.gregs.voidps.engine.data.param.codec

import world.gregs.config.ConfigReader
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer

class IntParam(id: Int) : ParamCodec<Int>(id) {
    override fun read(reader: Reader) = reader.readInt()
    override fun read(reader: ConfigReader) = reader.int()
    override fun write(writer: Writer, value: Int) {
        writer.writeInt(value)
    }
}
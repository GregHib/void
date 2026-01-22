package world.gregs.config.param.codec

import world.gregs.config.ConfigReader
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer

object StringListParam : ParamCodec<List<String>>() {
    override fun read(reader: Reader) = List(reader.readSmart()) { reader.readString() }
    override fun read(reader: ConfigReader): List<String> {
        val list = mutableListOf<String>()
        while (reader.nextElement()) {
            list.add(reader.string())
        }
        return list
    }

    override fun write(writer: Writer, value: List<String>) {
        writer.writeSmart(value.size)
        for (element in value) {
            writer.writeString(element)
        }
    }
}
package world.gregs.voidps.engine.data.types.keys

import world.gregs.config.ConfigReader
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer

sealed class ParamKey<T>(
    val id: Int,
    val default: T,
) {
    abstract fun read(config: ConfigReader): T
    abstract fun read(reader: Reader): T
    abstract fun write(writer: Writer, value: Any)
}

sealed class ParamIntegerKey(id: Int, default: Int) : ParamKey<Int>(id, default) {
    override fun read(config: ConfigReader) = config.int()

    override fun read(reader: Reader) = reader.readInt()

    override fun write(writer: Writer, value: Any) {
        writer.writeInt(value as Int)
    }
}
